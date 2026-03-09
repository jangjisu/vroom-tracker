package com.vroomtracker.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 실시간 전국 교통량 항목 */
@Getter
@Setter
@NoArgsConstructor
public class TrafficAllItem {
    /** 도공/민자 구분명 */
    private String exDivName;
    /** 자료구분명 */
    private String tmName;
    /** TCS/hi-pass 구분명 */
    private String tcsName;
    /**
     * 교통량
     * ※ API 문서에 'trafficAmout' (오타) 로 명시되어 있음.
     *   실제 응답 키가 다를 경우 @JsonProperty 값을 수정하세요.
     */
    @JsonProperty("trafficAmout")
    private String trafficAmout;
    /** 집계시간 */
    private String sumTm;
    /** 차종구분코드 */
    private String carType;
    /** 도공/민자 구분코드 */
    private String exDivCode;
    /** TCS/hi-pass 구분 */
    private String tcsType;
    /** 자료구분 */
    private String tmType;
}
