package com.vroomtracker.dto;

import lombok.Builder;
import lombok.Getter;

/** 전국 교통량 요약 뷰 모델 (상단 카드용) */
@Getter
@Builder
public class NationwideTrafficDto {

    /** 전국 총 교통량 (대) */
    private final String totalVolume;

    /** 교통량 집계시간 (sumTm) */
    private final String sumTm;

    /** 정체(혼잡) 영업소 수 */
    private final int congestedSections;

    /** 가장 붐비는 영업소명 */
    private final String busiestPlace;

    /** 가장 붐비는 영업소 교통량 (대) */
    private final String busiestVolume;

    /**
     * 집계된 값들로부터 요약 DTO를 생성합니다.
     * totalVolume 포맷 변환은 내부에서 처리합니다.
     */
    public static NationwideTrafficDto of(double totalVol, int congestedSections, String sumTm,
                                          String busiestPlace, String busiestVolume) {
        return NationwideTrafficDto.builder()
                .totalVolume(String.format("%.0f 대", totalVol))
                .sumTm(sumTm)
                .congestedSections(congestedSections)
                .busiestPlace(busiestPlace)
                .busiestVolume(busiestVolume)
                .build();
    }
}
