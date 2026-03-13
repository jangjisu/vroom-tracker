package com.vroomtracker.client;

import com.vroomtracker.client.response.TrafficAllResponse;
import com.vroomtracker.client.response.TrafficFlowResponse;
import com.vroomtracker.client.response.TrafficIcResponse;
import com.vroomtracker.client.response.TrafficRegionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
     * 권역별 교통량
     * URL: /openapi/trafficapi/trafficRegion
     *
     * @param key        인증키 (필수)
     * @param type       포맷 json/xml (필수)
     * @param exDivCode  도공/민자 구분코드 (선택)
     * @param regionCode 권역코드 (선택)
     * @param inoutType  0:입구, 1:출구 (선택)
     * @param tcsType    1:TCS, 2:hi-pass (선택)
     * @param carType    차종구분코드 (선택)
     * @param openClType 0:폐쇄식, 1:개방식 (선택)
     * @param tmType     1:1시간, 2:15분, 3:5분 (선택)
     */
    @GetMapping("/openapi/trafficapi/trafficRegion")
    TrafficRegionResponse getTrafficRegion(
            @RequestParam("key") String key,
            @RequestParam("type") String type,
            @RequestParam(value = "exDivCode", required = false) String exDivCode,
            @RequestParam(value = "regionCode", required = false) String regionCode,
            @RequestParam(value = "inoutType", required = false) String inoutType,
            @RequestParam(value = "tcsType", required = false) String tcsType,
            @RequestParam(value = "carType", required = false) String carType,
            @RequestParam(value = "openClType", required = false) String openClType,
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
}
