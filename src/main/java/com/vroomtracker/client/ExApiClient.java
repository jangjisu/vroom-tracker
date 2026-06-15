package com.vroomtracker.client;

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

    private static final String LOCATION_INFO_REST_PATH = "/openapi/locationinfo/locationinfoRest";
    private static final String CONVENIENCE_SERVICE_AREA_PATH = "/openapi/business/conveniServiceArea";
    private static final String HIGHWAY_SERVICE_AREA_INFO_PATH = "/openapi/restinfo/hiwaySvarInfoList";
    private static final String REST_STOP_NUM_OF_ROWS = "99";

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
                .queryParam("numOfRows", REST_STOP_NUM_OF_ROWS)
                .queryParam("pageNo", pageNumber)
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
                .queryParam("numOfRows", REST_STOP_NUM_OF_ROWS)
                .queryParam("pageNo", pageNumber)
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
                .queryParam("key", apiKey)
                .queryParam("type", ExApiResponseFormat.JSON.value());
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
