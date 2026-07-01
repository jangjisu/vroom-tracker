package com.restroute.service;

import com.restroute.client.ExApiClient;
import com.restroute.client.response.RestOilItem;
import com.restroute.client.response.RestOilResponse;
import com.restroute.domain.RestOilEntity;
import com.restroute.repository.RestOilRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class RestOilSyncService {

    private final ExApiClient exApiClient;
    private final RestOilRepository restOilRepository;
    private final TransactionTemplate transactionTemplate;

    public int initializeRestOilsIfEmpty() {
        if (restOilRepository.count() > 0) {
            return 0;
        }

        return refreshRestOils();
    }

    public int refreshRestOils() {
        List<RestOilItem> items = fetchRestOils();

        transactionTemplate.executeWithoutResult(status -> replaceRestOils(items));

        return items.size();
    }

    private List<RestOilItem> fetchRestOils() {
        RestOilResponse response = exApiClient.getRestOilList();
        if (response.getList() == null) {
            return List.of();
        }

        return response.getList();
    }

    private void replaceRestOils(List<RestOilItem> items) {
        restOilRepository.deleteAllInBatch();
        restOilRepository.saveAll(items.stream().map(RestOilEntity::from).toList());
    }
}
