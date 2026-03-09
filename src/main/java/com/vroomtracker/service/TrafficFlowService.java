package com.vroomtracker.service;

import com.vroomtracker.client.ExApiClient;
import com.vroomtracker.client.response.TrafficFlowItem;
import com.vroomtracker.client.response.TrafficFlowResponse;
import com.vroomtracker.domain.TrafficFlowEntity;
import com.vroomtracker.dto.TrafficFlowDto;
import com.vroomtracker.repository.TrafficFlowRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficFlowService {

    private final TrafficFlowRepository trafficFlowRepository;
    private final ExApiClient exApiClient;

    @Value("${ex.api.key}")
    private String apiKey;

    /**
     * 앱 시작 시 DB에 현재 연도 데이터가 없으면 API에서 초기 적재합니다.
     */
    @PostConstruct
    public void initialize() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        if (trafficFlowRepository.countByStdYear(year) == 0) {
            log.info("DB에 {}년 trafficFlow 데이터 없음, API 초기 적재 시작", year);
            refreshByYear(year);
        }
    }

    /**
     * 주어진 연도의 시간대별 교통량 데이터를 DB에서 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<TrafficFlowDto> findByYear(String year) {
        return trafficFlowRepository.findByStdYear(year).stream()
                .map(TrafficFlowDto::from)
                .toList();
    }

    /**
     * API에서 새 데이터를 가져와 해당 연도 데이터를 교체합니다.
     * 빈 응답이면 기존 데이터를 유지합니다.
     */
    // @Transactional 제거 — 외부 API 호출이 포함되어 있음
    public void refreshByYear(String year) {
        List<TrafficFlowItem> items = fetchFromApi(year);
        if (items.isEmpty()) {
            log.warn("{}년 trafficFlowByTime API 결과 없음, 기존 데이터 유지", year);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<TrafficFlowEntity> entities = items.stream()
                .map(item -> TrafficFlowEntity.builder()
                        .stdYear(item.getStdYear())
                        .sphlDfttNm(item.getSphlDfttNm())
                        .sphlDfttCode(item.getSphlDfttCode())
                        .sphlDfttScopTypeNm(item.getSphlDfttScopTypeNm())
                        .sphlDfttScopTypeCode(item.getSphlDfttScopTypeCode())
                        .stdHour(parseHour(item.getStdHour()))
                        .trfl(parseTraffic(item.getTrfl()))
                        .fetchedAt(now)
                        .build())
                .toList();
        saveFlow(year, entities);
    }

    @Transactional
    void saveFlow(String year, List<TrafficFlowEntity> entities) {
        trafficFlowRepository.deleteByStdYear(year);
        trafficFlowRepository.saveAll(entities);
        log.info("{}년 trafficFlow {}건 저장 완료", year, entities.size());
    }

    private static int parseHour(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private static long parseTraffic(String s) {
        try { return Long.parseLong(s.trim()); } catch (Exception e) { return 0L; }
    }

    private List<TrafficFlowItem> fetchFromApi(String year) {
        try {
            TrafficFlowResponse response =
                    exApiClient.getTrafficFlowByTime(apiKey, "json", year);

            if (!"00".equals(response.getCode())) {
                log.warn("trafficFlowByTime API 실패: code={}", response.getCode());
                return Collections.emptyList();
            }

            List<TrafficFlowItem> list = response.getList();
            return list != null ? list : Collections.emptyList();

        } catch (Exception e) {
            log.error("trafficFlowByTime API 호출 실패", e);
            return Collections.emptyList();
        }
    }
}
