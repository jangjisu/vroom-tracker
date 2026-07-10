package com.restroute.service;

import com.restroute.client.ExApiClient;
import com.restroute.client.response.RepresentativeFoodItem;
import com.restroute.client.response.RepresentativeFoodResponse;
import com.restroute.domain.RepresentativeFoodEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.RepresentativeFoodRepository;
import com.restroute.repository.RestStopRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class RepresentativeFoodSyncService {

    private static final int FIRST_PAGE = 1;

    private final ExApiClient exApiClient;
    private final RepresentativeFoodRepository representativeFoodRepository;
    private final RestStopRepository restStopRepository;
    private final TransactionTemplate transactionTemplate;

    public int initializeRepresentativeFoodsIfEmpty() {
        if (representativeFoodRepository.count() > 0) {
            return 0;
        }

        return refreshRepresentativeFoods();
    }

    public int refreshRepresentativeFoods() {
        List<RepresentativeFoodItem> items = fetchAllPages();
        Map<String, String> restStopCodes = restStopRepository.findAll().stream()
                .map(RestStopEntity::getServiceAreaCode)
                .filter(code -> code != null && !code.isBlank())
                .collect(java.util.stream.Collectors.toMap(code -> code, code -> code, (first, second) -> first));

        transactionTemplate.executeWithoutResult(status -> replaceAll(items, restStopCodes));
        return items.size();
    }

    private List<RepresentativeFoodItem> fetchAllPages() {
        RepresentativeFoodResponse firstPage = exApiClient.getRepresentativeFoodServiceArea(FIRST_PAGE);
        List<RepresentativeFoodItem> items = new ArrayList<>();
        addItems(items, firstPage);

        int totalPages = Math.max(firstPage.getPageSize(), FIRST_PAGE);
        for (int pageNo = FIRST_PAGE + 1; pageNo <= totalPages; pageNo++) {
            RepresentativeFoodResponse response = exApiClient.getRepresentativeFoodServiceArea(pageNo);
            addItems(items, response);
        }

        validateUniqueKeys(items);
        return items;
    }

    private void addItems(List<RepresentativeFoodItem> items, RepresentativeFoodResponse response) {
        if (response.getList() == null) {
            return;
        }

        items.addAll(response.getList());
    }

    private void validateUniqueKeys(List<RepresentativeFoodItem> items) {
        Set<String> keys = new HashSet<>();
        for (RepresentativeFoodItem item : items) {
            if (item.getServiceAreaCode() == null || item.getServiceAreaCode().isBlank()) {
                throw new IllegalStateException("대표 음식 응답에 serviceAreaCode가 없습니다.");
            }

            String key = item.getServiceAreaCode() + "\n" + item.getDirection();
            if (!keys.add(key)) {
                throw new IllegalStateException("대표 음식 자연키가 중복되었습니다: " + key);
            }
        }
    }

    private void replaceAll(List<RepresentativeFoodItem> items, Map<String, String> restStopCodes) {
        List<RepresentativeFoodEntity> entities = items.stream()
                .map(item -> RepresentativeFoodEntity.from(item, restStopCodes.get(item.getServiceAreaCode())))
                .toList();
        representativeFoodRepository.deleteAllInBatch();
        representativeFoodRepository.saveAll(entities);
    }
}
