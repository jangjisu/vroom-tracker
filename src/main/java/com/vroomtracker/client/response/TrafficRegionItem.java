package com.vroomtracker.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 권역별 교통량 항목 */
@Getter
@Setter
@NoArgsConstructor
public class TrafficRegionItem {
    /** 도공/민자 구분코드 */
    private String exDivCode;
    /** 도공/민자 구분명 */
    private String exDivName;
    /** 권역코드 */
    private String regionCode;
    /** 권역명 */
    private String regionName;
    /** 입출구 구분코드 (0:입구, 1:출구) */
    private String inoutType;
    /** 입출구 구분명 */
    private String inoutName;
    /** 자료구분 (1:1시간, 2:15분, 3:5분) */
    private String tmType;
    /** 자료구분명 */
    private String tmName;
    /** TCS/hi-pass 구분 */
    private String tcsType;
    /** TCS/hi-pass 구분명 */
    private String tcsName;
    /** 차종구분코드 */
    private String carType;
    /** 폐쇄/개방식 구분코드 (폐쇄식:0, 개방식:1) */
    private String openClType;
    /** 폐쇄/개방식 구분명 */
    private String openClName;
    /**
     * 교통량
     * ※ API 문서에 'trafficAmout' (오타) 로 명시되어 있음.
     *   실제 응답 키가 다를 경우 @JsonProperty 값을 수정하세요.
     */
    @JsonProperty("trafficAmout")
    private String trafficAmount;
    /** 집계시간 */
    private String sumTm;
    /** 집계일자 (yyyyMMdd) */
    private String sumDate;
}
