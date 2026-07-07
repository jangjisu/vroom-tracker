package com.restroute.client;

import static com.restroute.client.OpinetFeignClient.AVERAGE_ALL_PRICE_PATH;
import static com.restroute.client.OpinetFeignClient.CODE_PARAMETER;
import static com.restroute.client.OpinetFeignClient.OUT_PARAMETER;

import com.restroute.client.response.OpinetAverageOilPriceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OpinetApiClient {

    private static final String FORMAT_JSON = "json";

    private final OpinetFeignClient opinetFeignClient;
    private final String apiUrl;
    private final String apiKey;

    public OpinetApiClient(
            OpinetFeignClient opinetFeignClient,
            @Value("${opinet.api.url}") String apiUrl,
            @Value("${opinet.api.key}") String apiKey) {
        this.opinetFeignClient = opinetFeignClient;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    public OpinetAverageOilPriceResponse getAverageOilPrices() {
        String requestUrl = UriComponentsBuilder.fromUriString(apiUrl)
                .path(AVERAGE_ALL_PRICE_PATH)
                .queryParam(OUT_PARAMETER, FORMAT_JSON)
                .queryParam(CODE_PARAMETER, apiKey)
                .build()
                .encode()
                .toUriString();

        try {
            OpinetAverageOilPriceResponse response = opinetFeignClient.getAverageOilPrices(FORMAT_JSON, apiKey);
            if (response == null) {
                throw new ExApiException(requestUrl, "empty response");
            }
            if (!response.isSuccess()) {
                throw new ExApiException(requestUrl, response.getErrorMessage());
            }
            return response;
        } catch (ExApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ExApiException(requestUrl, e.getMessage(), e);
        }
    }
}
