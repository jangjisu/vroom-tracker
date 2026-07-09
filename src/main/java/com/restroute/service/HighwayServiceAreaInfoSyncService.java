package com.restroute.service;

import com.restroute.client.ExApiClient;
import com.restroute.client.response.HighwayServiceAreaInfoItem;
import com.restroute.client.response.HighwayServiceAreaInfoResponse;
import com.restroute.domain.HighwayServiceAreaInfoEntity;
import com.restroute.repository.HighwayServiceAreaInfoRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class HighwayServiceAreaInfoSyncService {

    private final ExApiClient exApiClient;
    private final HighwayServiceAreaInfoRepository highwayServiceAreaInfoRepository;
    private final RestStopServiceAreaCodeMappingService restStopServiceAreaCodeMappingService;
    private final TransactionTemplate transactionTemplate;

    public int refreshHighwayServiceAreaInfos() {
        List<HighwayServiceAreaInfoItem> items = fetchHighwayServiceAreaInfos();

        transactionTemplate.executeWithoutResult(status -> upsertHighwayServiceAreaInfos(items));

        return items.size();
    }

    private List<HighwayServiceAreaInfoItem> fetchHighwayServiceAreaInfos() {
        HighwayServiceAreaInfoResponse response = exApiClient.getHighwayServiceAreaInfoList();

        if (response.getList() == null) {
            return List.of();
        }

        return response.getList();
    }

    private void upsertHighwayServiceAreaInfos(List<HighwayServiceAreaInfoItem> items) {
        Map<String, String> restStopServiceAreaCodeByServiceAreaCode =
                restStopServiceAreaCodeMappingService.mapByServiceAreaCode();
        Map<String, HighwayServiceAreaInfoEntity> existingByKey = highwayServiceAreaInfoRepository.findAll().stream()
                .collect(Collectors.toMap(
                        HighwayServiceAreaInfoEntity::getServiceAreaCode, entity -> entity, (first, second) -> first));

        List<HighwayServiceAreaInfoEntity> toSave = new ArrayList<>();
        for (HighwayServiceAreaInfoItem item : items) {
            toSave.add(upsertOne(item, existingByKey, restStopServiceAreaCodeByServiceAreaCode));
        }

        highwayServiceAreaInfoRepository.saveAll(toSave);
    }

    private HighwayServiceAreaInfoEntity upsertOne(
            HighwayServiceAreaInfoItem item,
            Map<String, HighwayServiceAreaInfoEntity> existingByKey,
            Map<String, String> restStopServiceAreaCodeByServiceAreaCode) {
        HighwayServiceAreaInfoEntity existing = existingByKey.get(item.getServiceAreaCode());
        String restStopServiceAreaCode = restStopServiceAreaCodeByServiceAreaCode.get(item.getBusinessFacilityCode());

        if (existing == null) {
            HighwayServiceAreaInfoEntity created = HighwayServiceAreaInfoEntity.from(item);
            created.updateRestStopServiceAreaCode(restStopServiceAreaCode);
            existingByKey.put(item.getServiceAreaCode(), created);
            return created;
        }

        existing.updateFrom(item);
        existing.updateRestStopServiceAreaCode(restStopServiceAreaCode);
        return existing;
    }
}
