package com.vroomtracker.service;

import com.vroomtracker.client.ExApiClient;
import com.vroomtracker.client.response.RestOilPriceItem;
import com.vroomtracker.client.response.RestOilPriceResponse;
import com.vroomtracker.domain.RestOilPriceEntity;
import com.vroomtracker.repository.RestOilPriceRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class RestOilPriceSyncService {

    private static final int FIRST_PAGE = 1;
    private static final int LAST_PAGE = 3;

    private final ExApiClient exApiClient;
    private final RestOilPriceRepository restOilPriceRepository;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public int initializeRestOilPricesIfEmpty() {
        if (restOilPriceRepository.count() > 0) {
            return 0;
        }

        return refreshRestOilPrices();
    }

    public int refreshRestOilPrices() {
        List<RestOilPriceItem> items = fetchRestOilPrices();

        transactionTemplate.executeWithoutResult(status -> replaceRestOilPrices(items));

        return items.size();
    }

    private List<RestOilPriceItem> fetchRestOilPrices() {
        List<RestOilPriceItem> items = new ArrayList<>();
        for (int pageNo = FIRST_PAGE; pageNo <= LAST_PAGE; pageNo++) {
            addItems(items, exApiClient.getCurStateStation(pageNo));
        }

        return items;
    }

    private void addItems(List<RestOilPriceItem> items, RestOilPriceResponse response) {
        if (response.getList() == null) {
            return;
        }

        items.addAll(response.getList());
    }

    private void replaceRestOilPrices(List<RestOilPriceItem> items) {
        restOilPriceRepository.deleteAllInBatch();
        LocalDateTime refreshedAt = LocalDateTime.now(clock);
        restOilPriceRepository.saveAll(items.stream()
                .map(item -> RestOilPriceEntity.from(item, refreshedAt))
                .toList());
    }
}
