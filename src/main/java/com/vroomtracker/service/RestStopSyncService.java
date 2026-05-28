package com.vroomtracker.service;

import com.vroomtracker.client.ExApiClient;
import com.vroomtracker.client.response.RestStopItem;
import com.vroomtracker.client.response.RestStopResponse;
import com.vroomtracker.domain.RestStopEntity;
import com.vroomtracker.repository.RestStopRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class RestStopSyncService {

    private static final String FORMAT_JSON = "json";
    private static final String NUM_OF_ROWS = "99";
    private static final int FIRST_PAGE = 1;

    private final ExApiClient exApiClient;
    private final RestStopRepository restStopRepository;
    private final TransactionTemplate transactionTemplate;

    @Value("${ex.api.key}")
    private String apiKey;

    public int initializeRestStopsIfEmpty() {
        if (!needsInitialSync()) {
            return 0;
        }

        return refreshRestStops();
    }

    public int refreshRestStops() {
        List<RestStopItem> items = fetchAllRestStops();

        transactionTemplate.executeWithoutResult(status -> replaceRestStops(items));

        return items.size();
    }

    private List<RestStopItem> fetchAllRestStops() {
        RestStopResponse firstPage = fetchPage(FIRST_PAGE);
        List<RestStopItem> items = new ArrayList<>();
        addItems(items, firstPage);

        int pageSize = firstPage.getPageSizeAsInt();
        for (int pageNo = FIRST_PAGE + 1; pageNo <= pageSize; pageNo++) {
            addItems(items, fetchPage(pageNo));
        }

        return items;
    }

    private RestStopResponse fetchPage(int pageNo) {
        RestStopResponse response =
                exApiClient.getLocationInfoRest(apiKey, FORMAT_JSON, NUM_OF_ROWS, String.valueOf(pageNo));

        if (response == null || !response.isSuccess()) {
            throw new IllegalStateException("Failed to fetch rest stop page: " + pageNo);
        }

        return response;
    }

    private void addItems(List<RestStopItem> items, RestStopResponse response) {
        if (response.getList() != null) {
            items.addAll(response.getList());
        }
    }

    private void replaceRestStops(List<RestStopItem> items) {
        restStopRepository.deleteAllInBatch();
        restStopRepository.saveAll(items.stream().map(RestStopEntity::from).toList());
    }

    private boolean needsInitialSync() {
        return restStopRepository.count() == 0 || restStopRepository.countByXValueIsNullOrYValueIsNull() > 0;
    }
}
