package com.restroute.service;

import com.restroute.client.ExApiClient;
import com.restroute.client.response.RestStopItem;
import com.restroute.client.response.RestStopResponse;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.RestStopRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class RestStopSyncService {

    private static final int FIRST_PAGE = 1;

    private final ExApiClient exApiClient;
    private final RestStopRepository restStopRepository;
    private final TransactionTemplate transactionTemplate;

    public int initializeRestStopsIfEmpty() {
        if (restStopRepository.count() > 0) {
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

        int totalPageCount = firstPage.getTotalPageCount();
        for (int pageNo = FIRST_PAGE + 1; pageNo <= totalPageCount; pageNo++) {
            addItems(items, fetchPage(pageNo));
        }

        return items;
    }

    private RestStopResponse fetchPage(int pageNo) {
        return exApiClient.getLocationInfoRest(pageNo);
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
}
