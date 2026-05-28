package com.vroomtracker.client;

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
}
