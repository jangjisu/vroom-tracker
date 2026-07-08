package com.restroute.service;

import com.restroute.client.ExApiClient;
import com.restroute.client.response.RestStopDetailItem;
import com.restroute.client.response.RestStopDetailResponse;
import com.restroute.domain.RestStopDetailEntity;
import com.restroute.repository.RestStopDetailRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class RestStopDetailSyncService {

    private static final int FIRST_PAGE = 1;

    private final ExApiClient exApiClient;
    private final RestStopDetailRepository restStopDetailRepository;
    private final TransactionTemplate transactionTemplate;

    public int initializeRestStopDetailsIfEmpty() {
        if (restStopDetailRepository.count() > 0) {
            return 0;
        }

        return refreshRestStopDetails();
    }

    public int refreshRestStopDetails() {
        List<RestStopDetailItem> items = fetchAllRestStopDetails();

        transactionTemplate.executeWithoutResult(status -> upsertRestStopDetails(items));

        return items.size();
    }

    private List<RestStopDetailItem> fetchAllRestStopDetails() {
        RestStopDetailResponse firstPage = fetchPage(FIRST_PAGE);
        List<RestStopDetailItem> items = new ArrayList<>();
        addItems(items, firstPage);

        int totalPageCount = firstPage.getTotalPageCount();
        for (int pageNo = FIRST_PAGE + 1; pageNo <= totalPageCount; pageNo++) {
            addItems(items, fetchPage(pageNo));
        }

        return items;
    }

    private RestStopDetailResponse fetchPage(int pageNo) {
        return exApiClient.getConvenienceServiceArea(pageNo);
    }

    private void addItems(List<RestStopDetailItem> items, RestStopDetailResponse response) {
        if (response.getList() != null) {
            items.addAll(response.getList());
        }
    }

    private void upsertRestStopDetails(List<RestStopDetailItem> items) {
        Map<String, RestStopDetailEntity> existingByKey = restStopDetailRepository.findAll().stream()
                .collect(Collectors.toMap(RestStopDetailEntity::getServiceAreaCode, entity -> entity, (first, second) -> first));

        List<RestStopDetailEntity> toSave = new ArrayList<>();
        for (RestStopDetailItem item : items) {
            toSave.add(upsertOne(item, existingByKey));
        }

        restStopDetailRepository.saveAll(toSave);
    }

    private RestStopDetailEntity upsertOne(RestStopDetailItem item, Map<String, RestStopDetailEntity> existingByKey) {
        RestStopDetailEntity existing = existingByKey.get(item.getServiceAreaCode());

        if (existing == null) {
            RestStopDetailEntity created = RestStopDetailEntity.from(item);
            existingByKey.put(item.getServiceAreaCode(), created);
            return created;
        }

        existing.updateFrom(item);
        return existing;
    }
}
