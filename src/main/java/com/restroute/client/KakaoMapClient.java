package com.restroute.client;

import com.restroute.client.response.KakaoDirectionsResponse;
import com.restroute.client.response.KakaoLocalSearchResponse;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KakaoMapClient {

    private static final String DEFAULT_PRIORITY = "RECOMMEND";

    private final KakaoNaviFeignClient kakaoNaviFeignClient;
    private final KakaoLocalFeignClient kakaoLocalFeignClient;
    private final String apiKey;

    public KakaoMapClient(
            KakaoNaviFeignClient kakaoNaviFeignClient,
            KakaoLocalFeignClient kakaoLocalFeignClient,
            @Value("${kakao.rest-api-key:}") String apiKey) {
        this.kakaoNaviFeignClient = kakaoNaviFeignClient;
        this.kakaoLocalFeignClient = kakaoLocalFeignClient;
        this.apiKey = apiKey;
    }

    public KakaoLocalSearchResponse searchKeyword(String query) {
        return fetch("local keyword search", () -> kakaoLocalFeignClient.searchKeyword(authorization(), query));
    }

    public KakaoDirectionsResponse getDirections(String origin, String destination) {
        return fetch(
                "directions",
                () -> kakaoNaviFeignClient.getDirections(authorization(), origin, destination, DEFAULT_PRIORITY));
    }

    private String authorization() {
        return "KakaoAK " + apiKey;
    }

    private <T> T fetch(String requestDescription, Supplier<T> request) {
        try {
            T response = request.get();
            if (response == null) {
                throw new KakaoApiException(requestDescription, "empty response");
            }
            return response;
        } catch (KakaoApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new KakaoApiException(requestDescription, e.getMessage(), e);
        }
    }
}
