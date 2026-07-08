package com.restroute.service;

import com.restroute.client.ExApiClient;
import com.restroute.client.response.RestBestfoodItem;
import com.restroute.client.response.RestBestfoodResponse;
import com.restroute.domain.RestFoodEntity;
import com.restroute.repository.RestFoodRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

        transactionTemplate.executeWithoutResult(status -> upsertRestFoods(items));

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

    private void upsertRestFoods(List<RestBestfoodItem> items) {
        Map<FoodKey, RestFoodEntity> existingByKey = restFoodRepository.findAll().stream()
                .collect(Collectors.toMap(
                        entity -> new FoodKey(entity.getStdRestCd(), entity.getSeq()), entity -> entity));

        List<RestFoodEntity> toSave =
                items.stream().map(item -> upsertOne(item, existingByKey)).toList();

        restFoodRepository.saveAll(toSave);
    }

    private RestFoodEntity upsertOne(RestBestfoodItem item, Map<FoodKey, RestFoodEntity> existingByKey) {
        FoodKey key = new FoodKey(item.getStdRestCd(), item.getSeq());
        RestFoodEntity existing = existingByKey.get(key);

        if (existing == null) {
            return RestFoodEntity.from(item);
        }

        existing.updateFrom(item);
        return existing;
    }

    private record FoodKey(String stdRestCd, String seq) {}
}

