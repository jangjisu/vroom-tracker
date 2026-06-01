package com.vroomtracker.client;

import com.vroomtracker.client.response.RestStopDetailResponse;
import com.vroomtracker.client.response.RestStopResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 한국도로공사 공공데이터 Feign Client (data.ex.co.kr)
 */
@FeignClient(name = "ex-api", url = "${ex.api.url}")
public interface ExApiFeignClient {

    @GetMapping("/openapi/locationinfo/locationinfoRest")
    RestStopResponse getLocationInfoRest(
            @RequestParam("key") String key,
            @RequestParam("type") String type,
            @RequestParam(value = "numOfRows", required = false) String numOfRows,
            @RequestParam(value = "pageNo", required = false) String pageNo);

    @GetMapping("/openapi/business/conveniServiceArea")
    RestStopDetailResponse getConvenienceServiceArea(
            @RequestParam("key") String key,
            @RequestParam("type") String type,
            @RequestParam(value = "numOfRows", required = false) String numOfRows,
            @RequestParam(value = "pageNo", required = false) String pageNo);
}
