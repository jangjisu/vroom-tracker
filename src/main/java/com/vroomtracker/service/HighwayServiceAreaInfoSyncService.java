package com.vroomtracker.service;

import com.vroomtracker.client.ExApiClient;
import com.vroomtracker.client.response.HighwayServiceAreaInfoItem;
import com.vroomtracker.client.response.HighwayServiceAreaInfoResponse;
import com.vroomtracker.domain.HighwayServiceAreaInfoEntity;
import com.vroomtracker.repository.HighwayServiceAreaInfoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class HighwayServiceAreaInfoSyncService {

    private final ExApiClient exApiClient;
    private final HighwayServiceAreaInfoRepository highwayServiceAreaInfoRepository;
    private final TransactionTemplate transactionTemplate;

    public int refreshHighwayServiceAreaInfos() {
        List<HighwayServiceAreaInfoItem> items = fetchHighwayServiceAreaInfos();

        transactionTemplate.executeWithoutResult(status -> replaceHighwayServiceAreaInfos(items));

        return items.size();
    }

    private List<HighwayServiceAreaInfoItem> fetchHighwayServiceAreaInfos() {
        HighwayServiceAreaInfoResponse response = exApiClient.getHighwayServiceAreaInfoList();

        if (response == null || !response.isSuccess()) {
            throw new IllegalStateException("Failed to fetch highway service area info list");
        }

        if (response.getList() == null) {
            return List.of();
        }

        return response.getList();
    }

    private void replaceHighwayServiceAreaInfos(List<HighwayServiceAreaInfoItem> items) {
        highwayServiceAreaInfoRepository.deleteAllInBatch();
        highwayServiceAreaInfoRepository.saveAll(
                items.stream().map(HighwayServiceAreaInfoEntity::from).toList());
    }
}
