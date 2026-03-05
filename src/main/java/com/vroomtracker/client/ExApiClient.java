package com.vroomtracker.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 한국도로공사 공공데이터 Feign Client (data.ex.co.kr)
 *
 * ※ 응답 래퍼 구조 (list 필드명) 는 실제 API 호출 후 확인이 필요합니다.
 *   다를 경우 각 Response 클래스의 @JsonProperty("list") 를 수정하세요.
 */
@FeignClient(name = "ex-api", url = "${ex.api.url}")
public interface ExApiClient {

    /**
     * 톨게이트 입/출구 교통량
     * URL: /openapi/trafficapi/trafficIc
     *
     * @param key       인증키 (필수)
     * @param type      포맷 json/xml (필수)
     * @param tmType    자료구분 1:1시간, 2:15분 (필수)
     * @param inoutType 0:입구, 1:출구 (선택)
     * @param numOfRows 페이지당 결과 수 (선택)
     * @param pageNo    페이지 번호 (선택)
     */
    @GetMapping("/openapi/trafficapi/trafficIc")
    TrafficIcResponse getTrafficIc(
            @RequestParam("key") String key,
            @RequestParam("type") String type,
            @RequestParam("tmType") String tmType,
            @RequestParam(value = "inoutType", required = false) String inoutType,
            @RequestParam(value = "numOfRows", required = false) String numOfRows,
            @RequestParam(value = "pageNo", required = false) String pageNo
    );

    /**
     * 실시간 전국 교통량
     * URL: /openapi/trafficapi/trafficAll
     *
     * @param key    인증키 (필수)
     * @param type   포맷 json/xml (필수)
     * @param tmType 자료구분 1:1시간, 2:15분, 3:5분 (선택)
     */
    @GetMapping("/openapi/trafficapi/trafficAll")
    TrafficAllResponse getTrafficAll(
            @RequestParam("key") String key,
            @RequestParam("type") String type,
            @RequestParam(value = "tmType", required = false) String tmType
    );

    /**
     * 시간대별 교통량 현황 (연도별 통계)
     * URL: /openapi/specialAnal/trafficFlowByTime
     *
     * @param key      인증키 (필수)
     * @param type     포맷 json/xml (필수)
     * @param stdYear  기준년 (선택, 없으면 최신)
     */
    @GetMapping("/openapi/specialAnal/trafficFlowByTime")
    TrafficFlowResponse getTrafficFlowByTime(
            @RequestParam("key") String key,
            @RequestParam("type") String type,
            @RequestParam(value = "iStdYear", required = false) String stdYear
    );

    // =========================================================
    // Response Wrapper Types
    // =========================================================

    @Data
    class TrafficIcResponse {
        private String code;
        private String message;
        private String count;
        /** ※ 실제 응답의 list 필드명을 확인 후 수정하세요 */
        @JsonProperty("list")
        private List<TrafficIcItem> list;
    }

    @Data
    class TrafficAllResponse {
        private String code;
        private String message;
        private String count;
        @JsonProperty("list")
        private List<TrafficAllItem> list;
    }

    @Data
    class TrafficFlowResponse {
        private String code;
        private String message;
        private String count;
        @JsonProperty("list")
        private List<TrafficFlowItem> list;
    }

    // =========================================================
    // Item Types
    // =========================================================

    /** 톨게이트 입/출구 교통량 항목 */
    @Data
    class TrafficIcItem {
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
        /** 교통량 (단위: 만대) */
        private String trafficAmount;
        /** 집계시간 */
        private String sumTm;
    }

    /** 실시간 전국 교통량 항목 */
    @Data
    class TrafficAllItem {
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

    /** 시간대별 교통량 현황 항목 */
    @Data
    class TrafficFlowItem {
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
}
