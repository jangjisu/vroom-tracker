package com.vroomtracker.dto;

import com.vroomtracker.domain.TrafficFlowEntity;
import lombok.Builder;
import lombok.Getter;

/** 시간대별 교통량 현황 뷰 모델 */
@Getter
@Builder
public class TrafficFlowDto {

    /** 특수일 구분 (예: 평일, 토요일, 공휴일, 연휴) */
    private final String dayType;
    /** 특수일 전후 기간 범위 (예: 연휴 D-3, 당일, 연휴 D+1) */
    private final String periodRange;
    /** 기준 시각 (0~23) */
    private final String hour;
    /** 교통량 원시값 (단위: 대) */
    private final String vehicleCount;
    /** 포맷된 교통량 (콤마 포함, 예: "1,234 대") */
    private final String formattedVehicleCount;

    public static TrafficFlowDto from(TrafficFlowEntity entity) {
        return TrafficFlowDto.builder()
                .dayType(entity.getSphlDfttNm())
                .periodRange(entity.getSphlDfttScopTypeNm())
                .hour(String.valueOf(entity.getStdHour()))
                .vehicleCount(String.valueOf(entity.getTrfl()))
                .formattedVehicleCount(formatVehicleCount(entity.getTrfl()))
                .build();
    }

    private static String formatVehicleCount(long trfl) {
        return String.format("%,d 대", trfl);
    }
}
