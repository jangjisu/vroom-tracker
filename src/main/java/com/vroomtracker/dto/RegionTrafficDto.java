package com.vroomtracker.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegionTrafficDto {

    /** 순위 */
    private final int rank;

    /** 권역코드 */
    private final String regionCode;

    /** 권역명 */
    private final String regionName;

    /** 총 교통량 (대) */
    private final long totalVolume;

    /** 총 교통량 표시용 문자열 */
    private final String formattedVolume;

    /** 막대그래프 너비 0~100 (최대값 기준 비율) */
    private final int barWidth;

    /** 집계시간 */
    private final String sumTm;

    /**
     * 서비스에서 컬렉션 수준 계산(rank, totalVolume, maxVol, sumTm 포맷) 완료 후 호출.
     */
    public static RegionTrafficDto of(int rank, String regionCode, String regionName,
                                      long totalVolume, long maxVolume, String formattedSumTm) {
        return RegionTrafficDto.builder()
                .rank(rank)
                .regionCode(regionCode)
                .regionName(regionName)
                .totalVolume(totalVolume)
                .formattedVolume(String.format("%,d 대", totalVolume))
                .barWidth(maxVolume > 0 ? (int) (totalVolume * 100 / maxVolume) : 0)
                .sumTm(formattedSumTm)
                .build();
    }
}
