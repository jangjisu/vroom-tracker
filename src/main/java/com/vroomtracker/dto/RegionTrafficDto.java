package com.vroomtracker.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegionTrafficDto {

    /** 순위 (출구 교통량 기준) */
    private final int rank;

    /** 권역코드 */
    private final String regionCode;

    /** 권역명 */
    private final String regionName;

    /** 입구 교통량 (대) */
    private final long entranceVolume;

    /** 출구 교통량 (대) */
    private final long exitVolume;

    /** 입구 교통량 표시용 문자열 */
    private final String formattedEntranceVolume;

    /** 출구 교통량 표시용 문자열 */
    private final String formattedExitVolume;

    /** 막대그래프 너비 0~100 (출구 최대값 기준 비율) */
    private final int barWidth;

    /** 집계시간 */
    private final String sumTm;

    /**
     * 서비스에서 입구·출구 합산, 순위, maxExitVolume 계산 완료 후 호출.
     */
    public static RegionTrafficDto of(int rank, String regionCode, String regionName,
                                      long entranceVolume, long exitVolume,
                                      long maxExitVolume, String formattedSumTm) {
        return RegionTrafficDto.builder()
                .rank(rank)
                .regionCode(regionCode)
                .regionName(regionName)
                .entranceVolume(entranceVolume)
                .exitVolume(exitVolume)
                .formattedEntranceVolume(String.format("%,d 대", entranceVolume))
                .formattedExitVolume(String.format("%,d 대", exitVolume))
                .barWidth(maxExitVolume > 0 ? (int) (exitVolume * 100 / maxExitVolume) : 0)
                .sumTm(formattedSumTm)
                .build();
    }
}
