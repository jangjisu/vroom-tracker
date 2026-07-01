package com.restroute.client;

import static com.restroute.client.ExApiFeignClient.CONVENIENCE_SERVICE_AREA_PATH;
import static com.restroute.client.ExApiFeignClient.CUR_STATE_STATION_PATH;
import static com.restroute.client.ExApiFeignClient.HIGHWAY_SERVICE_AREA_INFO_PATH;
import static com.restroute.client.ExApiFeignClient.KEY_PARAMETER;
import static com.restroute.client.ExApiFeignClient.LOCATION_INFO_REST_PATH;
import static com.restroute.client.ExApiFeignClient.NUM_OF_ROWS_PARAMETER;
import static com.restroute.client.ExApiFeignClient.PAGE_NO_PARAMETER;
import static com.restroute.client.ExApiFeignClient.REST_BESTFOOD_LIST_PATH;
import static com.restroute.client.ExApiFeignClient.REST_OIL_LIST_PATH;
import static com.restroute.client.ExApiFeignClient.REST_STOP_NUM_OF_ROWS;
import static com.restroute.client.ExApiFeignClient.SERVICE_AREA_CODE2_PARAMETER;
import static com.restroute.client.ExApiFeignClient.TYPE_PARAMETER;

import com.restroute.client.response.ExApiResponse;
import com.restroute.client.response.HighwayServiceAreaInfoResponse;
import com.restroute.client.response.RestBestfoodResponse;
import com.restroute.client.response.RestOilPriceResponse;
import com.restroute.client.response.RestOilResponse;
import com.restroute.client.response.RestStopDetailResponse;
import com.restroute.client.response.RestStopResponse;
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

    public RestOilResponse getRestOilList() {
        String requestUrl = requestUrl(REST_OIL_LIST_PATH).build().encode().toUriString();

        return fetch(requestUrl, () -> exApiFeignClient.getRestOilList(apiKey, ExApiResponseFormat.JSON.value()));
    }

    public RestOilPriceResponse getCurStateStation(int pageNo) {
        String pageNumber = String.valueOf(pageNo);
        String requestUrl = requestUrl(CUR_STATE_STATION_PATH)
                .queryParam(NUM_OF_ROWS_PARAMETER, REST_STOP_NUM_OF_ROWS)
                .queryParam(PAGE_NO_PARAMETER, pageNumber)
                .build()
                .encode()
                .toUriString();

        return fetch(
                requestUrl,
                () -> exApiFeignClient.getCurStateStation(
                        apiKey, ExApiResponseFormat.JSON.value(), REST_STOP_NUM_OF_ROWS, pageNumber));
    }

    public RestOilPriceResponse getCurStateStationByServiceAreaCode2(String serviceAreaCode2) {
        String pageNumber = "1";
        String requestUrl = requestUrl(CUR_STATE_STATION_PATH)
                .queryParam(NUM_OF_ROWS_PARAMETER, REST_STOP_NUM_OF_ROWS)
                .queryParam(PAGE_NO_PARAMETER, pageNumber)
                .queryParam(SERVICE_AREA_CODE2_PARAMETER, serviceAreaCode2)
                .build()
                .encode()
                .toUriString();

        return fetch(
                requestUrl,
                () -> exApiFeignClient.getCurStateStation(
                        apiKey, ExApiResponseFormat.JSON.value(), REST_STOP_NUM_OF_ROWS, pageNumber, serviceAreaCode2));
    }

    public RestBestfoodResponse getRestBestfoodList(int pageNo) {
        String pageNumber = String.valueOf(pageNo);
        String requestUrl = requestUrl(REST_BESTFOOD_LIST_PATH)
                .queryParam(NUM_OF_ROWS_PARAMETER, REST_STOP_NUM_OF_ROWS)
                .queryParam(PAGE_NO_PARAMETER, pageNumber)
                .build()
                .encode()
                .toUriString();

        return fetch(
                requestUrl,
                () -> exApiFeignClient.getRestBestfoodList(
                        apiKey, ExApiResponseFormat.JSON.value(), REST_STOP_NUM_OF_ROWS, pageNumber));
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
