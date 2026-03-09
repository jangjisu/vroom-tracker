package com.vroomtracker.client.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 시간대별 교통량 현황 항목 */
@Getter
@Setter
@NoArgsConstructor
public class TrafficFlowItem {
    /** 기준년 */
    private String stdYear;
    /** 특수일구분명 (평일, 토요일, 공휴일, 연휴 등) */
    private String sphlDfttNm;
    /** 특수일구분코드 */
    private String sphlDfttCode;
    /** 특수일전후특송기간범위구분명 */
    private String sphlDfttScopTypeNm;
    /** 특수일전후특송기간범위구분코드 */
    private String sphlDfttScopTypeCode;
    /** 기준시 (0~23) */
    private String stdHour;
    /** 교통량(대) */
    private String trfl;
}
