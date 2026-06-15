package com.vroomtracker.client;

import static com.vroomtracker.client.ExApiFeignClient.CONVENIENCE_SERVICE_AREA_PATH;
import static com.vroomtracker.client.ExApiFeignClient.HIGHWAY_SERVICE_AREA_INFO_PATH;
import static com.vroomtracker.client.ExApiFeignClient.KEY_PARAMETER;
import static com.vroomtracker.client.ExApiFeignClient.LOCATION_INFO_REST_PATH;
import static com.vroomtracker.client.ExApiFeignClient.NUM_OF_ROWS_PARAMETER;
import static com.vroomtracker.client.ExApiFeignClient.PAGE_NO_PARAMETER;
import static com.vroomtracker.client.ExApiFeignClient.REST_STOP_NUM_OF_ROWS;
import static com.vroomtracker.client.ExApiFeignClient.TYPE_PARAMETER;

import com.vroomtracker.client.response.ExApiResponse;
import com.vroomtracker.client.response.HighwayServiceAreaInfoResponse;
import com.vroomtracker.client.response.RestStopDetailResponse;
import com.vroomtracker.client.response.RestStopResponse;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ExApiClient {

    private final ExApiFeignClient exApiFeignClient;
    private final String apiUrl;
    private final String apiKey;

    public ExApiClient(
            ExApiFeignClient exApiFeignClient,
            @Value("${ex.api.url}") String apiUrl,
            @Value("${ex.api.key}") String apiKey) {
        this.exApiFeignClient = exApiFeignClient;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    public RestStopResponse getLocationInfoRest(int pageNo) {
        String pageNumber = String.valueOf(pageNo);
        String requestUrl = requestUrl(LOCATION_INFO_REST_PATH)
                .queryParam(NUM_OF_ROWS_PARAMETER, REST_STOP_NUM_OF_ROWS)
                .queryParam(PAGE_NO_PARAMETER, pageNumber)
                .build()
                .encode()
                .toUriString();

        return fetch(
                requestUrl,
                () -> exApiFeignClient.getLocationInfoRest(
                        apiKey, ExApiResponseFormat.JSON.value(), REST_STOP_NUM_OF_ROWS, pageNumber));
    }

    public RestStopDetailResponse getConvenienceServiceArea(int pageNo) {
        String pageNumber = String.valueOf(pageNo);
        String requestUrl = requestUrl(CONVENIENCE_SERVICE_AREA_PATH)
                .queryParam(NUM_OF_ROWS_PARAMETER, REST_STOP_NUM_OF_ROWS)
                .queryParam(PAGE_NO_PARAMETER, pageNumber)
                .build()
                .encode()
                .toUriString();

        return fetch(
                requestUrl,
                () -> exApiFeignClient.getConvenienceServiceArea(
                        apiKey, ExApiResponseFormat.JSON.value(), REST_STOP_NUM_OF_ROWS, pageNumber));
    }

    public HighwayServiceAreaInfoResponse getHighwayServiceAreaInfoList() {
        String requestUrl =
                requestUrl(HIGHWAY_SERVICE_AREA_INFO_PATH).build().encode().toUriString();

        return fetch(
                requestUrl,
                () -> exApiFeignClient.getHighwayServiceAreaInfoList(apiKey, ExApiResponseFormat.JSON.value()));
    }

    private UriComponentsBuilder requestUrl(String path) {
        return UriComponentsBuilder.fromUriString(apiUrl)
                .path(path)
                .queryParam(KEY_PARAMETER, apiKey)
                .queryParam(TYPE_PARAMETER, ExApiResponseFormat.JSON.value());
    }

    private <T extends ExApiResponse> T fetch(String requestUrl, Supplier<T> request) {
        try {
            T response = request.get();
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
