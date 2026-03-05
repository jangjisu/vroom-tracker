package com.vroomtracker.dto;

import com.vroomtracker.domain.TrafficFlowEntity;
import lombok.Builder;
import lombok.Getter;

/** 시간대별 교통량 현황 뷰 모델 */
@Getter
@Builder
public class TrafficFlowDto {

    private final String sphlDfttNm;
    private final String sphlDfttScopTypeNm;
    private final String stdHour;
    private final String trfl;
    /** 포맷된 교통량 (콤마 포함, 예: "1,234 대") */
    private final String formattedTrfl;

    public static TrafficFlowDto from(TrafficFlowEntity entity) {
        return TrafficFlowDto.builder()
                .sphlDfttNm(entity.getSphlDfttNm())
                .sphlDfttScopTypeNm(entity.getSphlDfttScopTypeNm())
                .stdHour(entity.getStdHour())
                .trfl(entity.getTrfl())
                .formattedTrfl(formatTrfl(entity.getTrfl()))
                .build();
    }

    private static String formatTrfl(String trfl) {
        try {
            return String.format("%,d 대", Long.parseLong(trfl.trim()));
        } catch (Exception e) {
            return (trfl != null ? trfl : "0") + " 대";
        }
    }
}
