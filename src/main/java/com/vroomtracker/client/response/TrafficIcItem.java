package com.vroomtracker.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 톨게이트 입/출구 교통량 항목 */
@Getter
@Setter
@NoArgsConstructor
public class TrafficIcItem {
    /** 도공/민자 구분코드 */
    private String exDivCode;
    /** 도공/민자 구분명 */
    private String exDivName;
    /** 영업소코드 */
    private String unitCode;
    /** 영업소명 */
    private String unitName;
    /** 입출구 구분코드 (0:입구, 1:출구) */
    private String inoutType;
    /** 입출구 구분명 */
    private String inoutName;
    /** 자료구분 (1:1시간, 2:15분) */
    private String tmType;
    /** 자료구분명 */
    private String tmName;
    /** TCS/hi-pass 구분 */
    private String tcsType;
    /** TCS/hi-pass 구분명 */
    private String tcsName;
    /** 차종구분코드 */
    private String carType;
    /** 교통량 (단위: 대) — API 응답 필드명 오타(trafficAmout) 보정 */
    @JsonProperty("trafficAmout")
    private String trafficAmount;
    /** 집계시간 */
    private String sumTm;
}
