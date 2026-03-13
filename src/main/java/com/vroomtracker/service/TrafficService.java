package com.vroomtracker.service;

import com.vroomtracker.client.ExApiClient;
import com.vroomtracker.client.InoutType;
import com.vroomtracker.client.TmType;
import com.vroomtracker.client.response.TrafficIcItem;
import com.vroomtracker.client.response.TrafficIcResponse;
import com.vroomtracker.client.response.TrafficRegionItem;
import com.vroomtracker.client.response.TrafficRegionResponse;
import com.vroomtracker.domain.CongestionLevel;
import com.vroomtracker.dto.DashboardData;
import com.vroomtracker.dto.NationwideTrafficDto;
import com.vroomtracker.dto.RegionTrafficDto;
import com.vroomtracker.dto.TollGateTrafficDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficService {

    private final ExApiClient exApiClient;

    @Value("${ex.api.key}")
    private String apiKey;

    @Value("${traffic.congestion.high-threshold}")
    private double highThreshold;

    @Value("${traffic.congestion.medium-threshold}")
    private double mediumThreshold;

    private static final String JSON = "json";
    private static final String PAGE_FIRST = "1";

    @Value("${ex.api.traffic-ic.num-of-rows}")
    private String numOfRows;

    /**
     * 대시보드 전체 데이터를 조합해 반환합니다.
     *
     * P1 수정: @Cacheable 을 getDashboardData() 에만 선언합니다.
     * - 개별 fetch 메서드에 @Cacheable 을 달고 this.fetch() 로 내부 호출하면
     *   Spring AOP 프록시를 우회해 캐시가 실제로 동작하지 않습니다.
     * - 이 메서드는 Controller 가 Bean 프록시를 통해 호출하므로 캐시가 정상 동작합니다.
     */
    @Cacheable(value = "dashboard", key = "#topN")
    public DashboardData getDashboardData(int topN) {
        List<TrafficIcItem> exitItems = fetchExitTraffic();
        List<TollGateTrafficDto> ranking = buildRanking(exitItems, topN);
        NationwideTrafficDto summary = buildSummary(exitItems, ranking);
        return new DashboardData(summary, ranking);
    }

    @Cacheable(value = "regionRanking")
    public List<RegionTrafficDto> getRegionRanking() {
        List<TrafficRegionItem> items = fetchRegionTraffic();
        return buildRegionRanking(items);
    }

    // ================================================================
    // API 호출 (private — 캐시는 getDashboardData 레벨에서만 처리)
    // ================================================================

    private List<TrafficIcItem> fetchExitTraffic() {
        try {
            TrafficIcResponse response =
                    exApiClient.getTrafficIc(apiKey, JSON,
                            TmType.FIFTEEN_MIN.value(), InoutType.EXIT.value(),
                            numOfRows, PAGE_FIRST);

            if (!response.isSuccess()) {
                log.warn("trafficIc API 실패: code={}, message={}", response.getCode(), response.getMessage());
                return Collections.emptyList();
            }

            List<TrafficIcItem> list = response.getList();
            return list != null ? list : Collections.emptyList();

        } catch (Exception e) {
            log.error("trafficIc API 호출 실패", e);
            return Collections.emptyList();
        }
    }

    private List<TrafficRegionItem> fetchRegionTraffic() {
        try {
            TrafficRegionResponse response =
                    exApiClient.getTrafficRegion(apiKey, JSON,
                            null, null, InoutType.EXIT.value(),
                            null, null, null, TmType.ONE_HOUR.value());

            if (!response.isSuccess()) {
                log.warn("trafficRegion API 실패: code={}, message={}", response.getCode(), response.getMessage());
                return Collections.emptyList();
            }

            List<TrafficRegionItem> list = response.getList();
            return list != null ? list : Collections.emptyList();

        } catch (Exception e) {
            log.error("trafficRegion API 호출 실패", e);
            return Collections.emptyList();
        }
    }

    // ================================================================
    // 데이터 가공
    // ================================================================

    private List<TollGateTrafficDto> buildRanking(List<TrafficIcItem> exitItems, int topN) {
        List<TrafficIcItem> validItems = exitItems.stream()
                .filter(i -> i.getTrafficAmount() != null && !i.getTrafficAmount().isBlank())
                .filter(i -> i.getInoutType() == null || "1".equals(i.getInoutType()))
                .toList();

        if (validItems.isEmpty()) return Collections.emptyList();

        // unitCode 기준 집계: 차종·TCS·시간대 중복 행을 합산
        record UnitSummary(TrafficIcItem rep, double totalVol) {}

        List<UnitSummary> aggregated = validItems.stream()
                .collect(Collectors.groupingBy(TrafficIcItem::getUnitCode))
                .values().stream()
                .map(group -> {
                    double total = group.stream()
                            .mapToDouble(i -> parseAmount(i.getTrafficAmount()))
                            .sum();
                    // 대표 행: 가장 최근 집계시간 기준
                    TrafficIcItem rep = group.stream()
                            .max(Comparator.comparing(i -> i.getSumTm() != null ? i.getSumTm() : ""))
                            .orElse(group.get(0));
                    return new UnitSummary(rep, total);
                })
                .sorted(Comparator.comparingDouble(UnitSummary::totalVol).reversed())
                .limit(topN)
                .toList();

        double maxVol = aggregated.get(0).totalVol();

        return IntStream.range(0, aggregated.size())
                .mapToObj(i -> {
                    UnitSummary us = aggregated.get(i);
                    return TollGateTrafficDto.from(
                            us.rep(), i + 1, us.totalVol(), maxVol,
                            congestionLevel(us.totalVol()), congestionLabel(us.totalVol()),
                            formatSumTm(us.rep().getSumTm())
                    );
                })
                .toList();
    }

    private NationwideTrafficDto buildSummary(List<TrafficIcItem> allItems,
                                              List<TollGateTrafficDto> ranking) {
        double totalVol = allItems.stream()
                .filter(i -> "1".equals(i.getInoutType()) || i.getInoutType() == null)
                .filter(i -> i.getTrafficAmount() != null && !i.getTrafficAmount().isBlank())
                .mapToDouble(i -> parseAmount(i.getTrafficAmount()))
                .sum();

        long congestedCount = allItems.stream()
                .filter(i -> i.getTrafficAmount() != null)
                .filter(i -> parseAmount(i.getTrafficAmount()) >= highThreshold)
                .count();

        String latestSumTm = allItems.stream()
                .filter(i -> i.getSumTm() != null)
                .map(TrafficIcItem::getSumTm)
                .max(Comparator.naturalOrder())
                .map(this::formatSumTm)
                .orElse("-");

        return NationwideTrafficDto.of(
                totalVol, (int) congestedCount, latestSumTm,
                ranking.isEmpty() ? "-" : ranking.get(0).getUnitName(),
                ranking.isEmpty() ? "0" : ranking.get(0).getFormattedVolume()
        );
    }

    private List<RegionTrafficDto> buildRegionRanking(List<TrafficRegionItem> items) {
        if (items.isEmpty()) return Collections.emptyList();

        record RegionSummary(String regionCode, String regionName, long totalVolume, String sumTm) {}

        List<RegionSummary> aggregated = items.stream()
                .filter(i -> i.getTrafficAmount() != null && !i.getTrafficAmount().isBlank())
                .collect(Collectors.groupingBy(TrafficRegionItem::getRegionCode))
                .entrySet().stream()
                .map(e -> {
                    List<TrafficRegionItem> group = e.getValue();
                    String regionName = group.get(0).getRegionName();
                    long total = group.stream()
                            .mapToLong(i -> (long) parseAmount(i.getTrafficAmount()))
                            .sum();
                    String rawSumTm = group.stream()
                            .map(i -> i.getSumDate() != null && i.getSumTm() != null
                                    ? i.getSumDate() + i.getSumTm() : "")
                            .filter(s -> !s.isBlank())
                            .max(Comparator.naturalOrder())
                            .orElse("-");
                    return new RegionSummary(e.getKey(), regionName, total, formatSumTm(rawSumTm));
                })
                .sorted(Comparator.comparingLong(RegionSummary::totalVolume).reversed())
                .toList();

        if (aggregated.isEmpty()) return Collections.emptyList();
        long maxVol = aggregated.get(0).totalVolume();

        return IntStream.range(0, aggregated.size())
                .mapToObj(i -> {
                    RegionSummary s = aggregated.get(i);
                    return RegionTrafficDto.of(i + 1, s.regionCode(), s.regionName(),
                            s.totalVolume(), maxVol, s.sumTm());
                })
                .toList();
    }

    // ================================================================
    // 유틸
    // ================================================================

    private double parseAmount(String amount) {
        try {
            return Double.parseDouble(amount.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String formatSumTm(String sumTm) {
        if (sumTm == null || sumTm.length() < 10) return sumTm;
        try {
            if (sumTm.length() == 10) {
                return DateTimeFormatter.ofPattern("yyyy-MM-dd HH시")
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHH").parse(sumTm));
            }
            if (sumTm.length() == 12) {
                return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm").parse(sumTm));
            }
        } catch (Exception ignored) {}
        return sumTm;
    }

    private CongestionLevel congestionLevel(double vol) {
        if (vol >= highThreshold) return CongestionLevel.HIGH;
        if (vol >= mediumThreshold) return CongestionLevel.MEDIUM;
        return CongestionLevel.LOW;
    }

    private String congestionLabel(double vol) {
        if (vol >= highThreshold) return "많음";
        if (vol >= mediumThreshold) return "보통";
        return "적음";
    }
}
