package com.vroomtracker.service;

import com.vroomtracker.client.ExApiClient;
import com.vroomtracker.client.response.TrafficIcItem;
import com.vroomtracker.client.response.TrafficIcResponse;
import com.vroomtracker.domain.CongestionLevel;
import com.vroomtracker.dto.DashboardData;
import com.vroomtracker.dto.NationwideTrafficDto;
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

    // ================================================================
    // API 호출 (private — 캐시는 getDashboardData 레벨에서만 처리)
    // ================================================================

    private List<TrafficIcItem> fetchExitTraffic() {
        try {
            TrafficIcResponse response =
                    exApiClient.getTrafficIc(apiKey, JSON, "2", "1", "500", "1");

            if (!"00".equals(response.getCode())) {
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

    // ================================================================
    // 데이터 가공
    // ================================================================

    private List<TollGateTrafficDto> buildRanking(List<TrafficIcItem> exitItems, int topN) {
        List<TrafficIcItem> filtered = exitItems.stream()
                .filter(i -> i.getTrafficAmount() != null && !i.getTrafficAmount().isBlank())
                .filter(i -> i.getInoutType() == null || "1".equals(i.getInoutType()))
                .sorted(Comparator.comparingDouble((TrafficIcItem i) -> parseAmount(i.getTrafficAmount())).reversed())
                .limit(topN)
                .toList();

        if (filtered.isEmpty()) return Collections.emptyList();

        double maxVol = parseAmount(filtered.get(0).getTrafficAmount());

        return IntStream.range(0, filtered.size())
                .mapToObj(i -> {
                    TrafficIcItem item = filtered.get(i);
                    double vol = parseAmount(item.getTrafficAmount());
                    return TollGateTrafficDto.builder()
                            .rank(i + 1)
                            .unitCode(item.getUnitCode())
                            .unitName(item.getUnitName())
                            .exDivName(item.getExDivName())
                            .exitVolume(vol)
                            .formattedVolume(String.format("%.1f 만대", vol))
                            .sumTm(formatSumTm(item.getSumTm()))
                            .congestionLevel(congestionLevel(vol))
                            .congestionLabel(congestionLabel(vol))
                            .barWidth(maxVol > 0 ? (int) (vol / maxVol * 100) : 0)
                            .build();
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

        return NationwideTrafficDto.builder()
                .totalVolume(String.format("%.1f 만대", totalVol))
                .sumTm(latestSumTm)
                .congestedSections((int) congestedCount)
                .busiestPlace(ranking.isEmpty() ? "-" : ranking.get(0).getUnitName())
                .busiestVolume(ranking.isEmpty() ? "0" : ranking.get(0).getFormattedVolume())
                .build();
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
