package com.restroute.service.evcharger;

import com.restroute.domain.EvChargerStationMappingEntity;
import com.restroute.repository.EvChargerRepository;
import com.restroute.repository.EvChargerStationMappingRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EvChargerQueryService {

    private static final String ACTIVE = "N";

    private final EvChargerRepository evChargerRepository;
    private final EvChargerStationMappingRepository mappingRepository;

    @Transactional(readOnly = true)
    public Map<String, Integer> findActiveChargerCounts(Collection<String> serviceAreaCodes) {
        Set<String> validServiceAreaCodes =
                serviceAreaCodes.stream().filter(StringUtils::hasText).collect(Collectors.toSet());
        if (validServiceAreaCodes.isEmpty()) {
            return Map.of();
        }

        List<EvChargerStationMappingEntity> mappings =
                mappingRepository.findAllByRestStopServiceAreaCodeIn(validServiceAreaCodes);
        Set<String> statIds =
                mappings.stream().map(EvChargerStationMappingEntity::getStatId).collect(Collectors.toSet());
        if (statIds.isEmpty()) {
            return Map.of();
        }

        Map<String, String> serviceAreaCodeByStatId = mappings.stream()
                .collect(Collectors.toMap(
                        EvChargerStationMappingEntity::getStatId,
                        EvChargerStationMappingEntity::getRestStopServiceAreaCode,
                        (first, second) -> first));
        return evChargerRepository.findAllByStatIdInAndDelYn(statIds, ACTIVE).stream()
                .filter(charger -> serviceAreaCodeByStatId.containsKey(charger.getStatId()))
                .collect(Collectors.groupingBy(
                        charger -> serviceAreaCodeByStatId.get(charger.getStatId()),
                        Collectors.summingInt(charger -> 1)));
    }

    @Transactional(readOnly = true)
    public int findActiveChargerCount(String serviceAreaCode) {
        return findActiveChargerCounts(Set.of(serviceAreaCode)).getOrDefault(serviceAreaCode, 0);
    }
}
