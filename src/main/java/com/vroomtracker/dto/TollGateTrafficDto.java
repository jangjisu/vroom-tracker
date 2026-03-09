package com.vroomtracker.dto;

import com.vroomtracker.domain.CongestionLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TollGateTrafficDto {

    /** 순위 */
    private final int rank;

    /** 영업소코드 */
    private final String unitCode;

    /** 영업소명 */
    private final String unitName;

    /** 도공/민자 구분명 */
    private final String exDivName;

    /** 출구 교통량 (만대, 소수점 1자리) */
    private final double exitVolume;

    /** 출구 교통량 표시용 문자열 */
    private final String formattedVolume;

    /** 집계시간 */
    private final String sumTm;

    /** 혼잡도 코드: HIGH / MEDIUM / LOW */
    private final CongestionLevel congestionLevel;

    /** 혼잡도 한글 라벨: 많음 / 보통 / 적음 */
    private final String congestionLabel;

    /** 막대그래프 너비 0~100 (최대값 기준 비율) */
    private final int barWidth;
}
