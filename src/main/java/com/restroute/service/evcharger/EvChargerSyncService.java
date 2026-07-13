package com.restroute.service.evcharger;

import com.restroute.client.EvChargerApiClient;
import com.restroute.client.EvChargerFeignClient;
import com.restroute.client.response.EvChargerItem;
import com.restroute.client.response.EvChargerResponse;
import com.restroute.domain.EvChargerEntity;
import com.restroute.repository.EvChargerRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvChargerSyncService {

    private static final int FIRST_PAGE = 1;

    private final EvChargerApiClient evChargerApiClient;
    private final EvChargerRepository evChargerRepository;
    private final TransactionTemplate transactionTemplate;

    public int initializeEvChargersIfEmpty() {
        if (evChargerRepository.count() > 0) {
            return 0;
        }
        return refreshEvChargers();
    }

    public int refreshEvChargers() {
        List<EvChargerItem> items = fetchAllEvChargers();
        transactionTemplate.executeWithoutResult(status -> upsertEvChargers(items));
        log.info("EV charger sync completed. itemCount={}, stationCount={}", items.size(), uniqueStationCount(items));
        return items.size();
    }

    private List<EvChargerItem> fetchAllEvChargers() {
        EvChargerResponse firstPage = fetchPage(FIRST_PAGE);
        List<EvChargerItem> items = new ArrayList<>();
        addHighwayRestStopItems(items, firstPage);

        int totalPageCount = firstPage.getTotalPageCount();
        for (int pageNo = FIRST_PAGE + 1; pageNo <= totalPageCount; pageNo++) {
            EvChargerResponse response = fetchPage(pageNo);
            addHighwayRestStopItems(items, response);
        }
        return items;
    }

    private EvChargerResponse fetchPage(int pageNo) {
        log.info("EV charger page request started. pageNo={}", pageNo);
        try {
            EvChargerResponse response = evChargerApiClient.getChargerInfo(pageNo);
            log.info(
                    "EV charger page request succeeded. pageNo={}, totalCount={}, itemCount={}, "
                            + "highwayRestStopItemCount={}",
                    pageNo,
                    response.getTotalCount(),
                    response.getList().size(),
                    highwayRestStopItemCount(response));
            return response;
        } catch (RuntimeException e) {
            log.warn("EV charger page request failed. pageNo={}, cause={}", pageNo, e.getMessage(), e);
            throw e;
        }
    }

    private void addHighwayRestStopItems(List<EvChargerItem> items, EvChargerResponse response) {
        response.getList().stream()
                .filter(this::isHighwayRestStopCharger)
                .filter(this::hasNaturalKey)
                .forEach(items::add);
    }

    private boolean isHighwayRestStopCharger(EvChargerItem item) {
        return EvChargerFeignClient.HIGHWAY_REST_STOP_KIND_DETAIL.equals(item.getKindDetail());
    }

    private boolean hasNaturalKey(EvChargerItem item) {
        return StringUtils.hasText(item.getStatId()) && StringUtils.hasText(item.getChgerId());
    }

    private long highwayRestStopItemCount(EvChargerResponse response) {
        return response.getList().stream()
                .filter(this::isHighwayRestStopCharger)
                .count();
    }

    private void upsertEvChargers(List<EvChargerItem> items) {
        Map<String, EvChargerEntity> existingByKey = evChargerRepository.findAll().stream()
                .collect(Collectors.toMap(
                        entity -> chargerKey(entity.getStatId(), entity.getChgerId()),
                        entity -> entity,
                        (first, second) -> first));

        List<EvChargerEntity> toSave = new ArrayList<>();
        for (EvChargerItem item : items) {
            String key = chargerKey(item.getStatId(), item.getChgerId());
            EvChargerEntity existing = existingByKey.get(key);
            if (existing == null) {
                EvChargerEntity created = EvChargerEntity.from(item);
                existingByKey.put(key, created);
                toSave.add(created);
                continue;
            }
            existing.updateFrom(item);
            toSave.add(existing);
        }
        evChargerRepository.saveAll(toSave);
    }

    private long uniqueStationCount(List<EvChargerItem> items) {
        return items.stream().map(EvChargerItem::getStatId).distinct().count();
    }

    private String chargerKey(String statId, String chgerId) {
        return statId + "\n" + chgerId;
    }
}
