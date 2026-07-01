package com.restroute.service;

import com.restroute.client.KakaoMapClient;
import com.restroute.client.response.KakaoLocalSearchResponse;
import com.restroute.controller.response.PlaceCandidateResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private final KakaoMapClient kakaoMapClient;

    public List<PlaceCandidateResponse> search(String query) {
        KakaoLocalSearchResponse response = kakaoMapClient.searchKeyword(query);
        if (response.isEmpty()) {
            return List.of();
        }

        List<PlaceCandidateResponse> candidates = new ArrayList<>();
        for (KakaoLocalSearchResponse.Document document : response.documents()) {
            Double longitude = parseCoordinate(document.x());
            Double latitude = parseCoordinate(document.y());
            if (longitude == null || latitude == null) {
                continue;
            }
            candidates.add(PlaceCandidateResponse.of(document.label(), document.addressName(), latitude, longitude));
        }
        return candidates;
    }

    private Double parseCoordinate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
