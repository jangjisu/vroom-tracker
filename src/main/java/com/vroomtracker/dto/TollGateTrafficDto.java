package com.vroomtracker.dto;

import com.vroomtracker.client.response.TrafficIcItem;
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

    /** 출구 교통량 (대) */
    private final double exitVolume;

    /** 출구 교통량 표시용 문자열 */
    private final String formattedVolume;

    /** 집계시간 */
    private final String sumTm;

    /** 혼잡도 코드: HIGH / MEDIUM / LOW */
    private final CongestionLevel congestionLevel;

    /** 막대그래프 너비 0~100 (최대값 기준 비율) */
    private final int barWidth;

    /** 혼잡도 한글 라벨: 많음 / 보통 / 적음 — CongestionLevel에서 직접 조회 */
    public String getCongestionLabel() {
        return congestionLevel != null ? congestionLevel.label() : "-";
    }

    /**
     * API 응답 VO → DTO 변환.
     * 컬렉션 수준 계산(rank, exitVolume, maxVol, 혼잡도, 집계시간 포맷)은 서비스에서 처리 후 전달.
     */
    public static TollGateTrafficDto from(TrafficIcItem item, int rank, double exitVolume,
                                          double maxVol, CongestionLevel congestionLevel,
                                          String formattedSumTm) {
        return TollGateTrafficDto.builder()
                .rank(rank)
                .unitCode(item.getUnitCode())
                .unitName(item.getUnitName())
                .exDivName(item.getExDivName())
                .exitVolume(exitVolume)
                .formattedVolume(String.format("%.0f 대", exitVolume))
                .sumTm(formattedSumTm)
                .congestionLevel(congestionLevel)
                .barWidth(maxVol > 0 ? (int) (exitVolume / maxVol * 100) : 0)
                .build();
    }
}
