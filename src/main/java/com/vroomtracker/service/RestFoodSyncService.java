package com.vroomtracker.service;

import com.vroomtracker.client.ExApiClient;
import com.vroomtracker.client.response.RestBestfoodItem;
import com.vroomtracker.client.response.RestBestfoodResponse;
import com.vroomtracker.domain.RestFoodEntity;
import com.vroomtracker.repository.RestFoodRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class RestFoodSyncService {

    private static final int FIRST_PAGE = 1;

    private final ExApiClient exApiClient;
    private final RestFoodRepository restFoodRepository;
    private final TransactionTemplate transactionTemplate;

    public int initializeRestFoodsIfEmpty() {
        if (restFoodRepository.count() > 0) {
            return 0;
        }

        return refreshRestFoods();
    }

    public int refreshRestFoods() {
        List<RestBestfoodItem> items = fetchRestFoods();

        transactionTemplate.executeWithoutResult(status -> replaceRestFoods(items));

        return items.size();
    }

    private List<RestBestfoodItem> fetchRestFoods() {
        List<RestBestfoodItem> items = new ArrayList<>();
        RestBestfoodResponse firstPage = exApiClient.getRestBestfoodList(FIRST_PAGE);
        addItems(items, firstPage);

        int totalPages = firstPage.getPageSize();
        for (int pageNo = FIRST_PAGE + 1; pageNo <= totalPages; pageNo++) {
            addItems(items, exApiClient.getRestBestfoodList(pageNo));
        }

        return items;
    }

    private void addItems(List<RestBestfoodItem> items, RestBestfoodResponse response) {
        if (response.getList() == null) {
            return;
        }

        items.addAll(response.getList());
    }

    private void replaceRestFoods(List<RestBestfoodItem> items) {
        restFoodRepository.deleteAllInBatch();
        restFoodRepository.saveAll(items.stream().map(RestFoodEntity::from).toList());
    }
}
