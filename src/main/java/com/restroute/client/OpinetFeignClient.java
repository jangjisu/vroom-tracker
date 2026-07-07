package com.restroute.client;

import com.restroute.client.response.OpinetAverageOilPriceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "opinet-api", url = "${opinet.api.url}")
public interface OpinetFeignClient {

    String AVERAGE_ALL_PRICE_PATH = "/api/avgAllPrice.do";
    String OUT_PARAMETER = "out";
    String CODE_PARAMETER = "code";

    @GetMapping(AVERAGE_ALL_PRICE_PATH)
    OpinetAverageOilPriceResponse getAverageOilPrices(
            @RequestParam(OUT_PARAMETER) String out, @RequestParam(CODE_PARAMETER) String code);
}
