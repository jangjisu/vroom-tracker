package com.vroomtracker.dto;

import lombok.Builder;
import lombok.Getter;

/** 전국 교통량 요약 뷰 모델 (상단 카드용) */
@Getter
@Builder
public class NationwideTrafficDto {

    /** 전국 총 교통량 (만대 단위 그대로 표시) */
    private final String totalVolume;

    /** 교통량 집계시간 (sumTm) */
    private final String sumTm;

    /** 정체(혼잡) 영업소 수 */
    private final int congestedSections;

    /** 가장 붐비는 영업소명 */
    private final String busiestPlace;

    /** 가장 붐비는 영업소 교통량 (만대) */
    private final String busiestVolume;
}
