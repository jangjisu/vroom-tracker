package com.vroomtracker.client;

import com.vroomtracker.client.response.HighwayServiceAreaInfoResponse;
import com.vroomtracker.client.response.RestStopDetailResponse;
import com.vroomtracker.client.response.RestStopResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExApiClient {

    private static final String REST_STOP_NUM_OF_ROWS = "99";

    private final ExApiFeignClient exApiFeignClient;
    private final String apiKey;

    public ExApiClient(ExApiFeignClient exApiFeignClient, @Value("${ex.api.key}") String apiKey) {
        this.exApiFeignClient = exApiFeignClient;
        this.apiKey = apiKey;
    }

    public RestStopResponse getLocationInfoRest(int pageNo) {
        return exApiFeignClient.getLocationInfoRest(
                apiKey, ExApiResponseFormat.JSON.value(), REST_STOP_NUM_OF_ROWS, String.valueOf(pageNo));
    }

    public RestStopDetailResponse getConvenienceServiceArea(int pageNo) {
        return exApiFeignClient.getConvenienceServiceArea(
                apiKey, ExApiResponseFormat.JSON.value(), REST_STOP_NUM_OF_ROWS, String.valueOf(pageNo));
    }

    public HighwayServiceAreaInfoResponse getHighwayServiceAreaInfoList() {
        return exApiFeignClient.getHighwayServiceAreaInfoList(apiKey, ExApiResponseFormat.JSON.value());
    }
}
