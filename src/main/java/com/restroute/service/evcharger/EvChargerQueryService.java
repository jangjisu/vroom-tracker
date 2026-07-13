package com.restroute.service.evcharger;

import com.restroute.domain.EvChargerStationMappingEntity;
import com.restroute.repository.EvChargerRepository;
import com.restroute.repository.EvChargerStationMappingRepository;
import java.util.Collection;
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
    public Set<String> findMappedServiceAreaCodes(Collection<String> serviceAreaCodes) {
        Set<String> validServiceAreaCodes =
                serviceAreaCodes.stream().filter(StringUtils::hasText).collect(Collectors.toSet());
        if (validServiceAreaCodes.isEmpty()) {
            return Set.of();
        }

        return mappingRepository.findAllByRestStopServiceAreaCodeIn(validServiceAreaCodes).stream()
                .map(EvChargerStationMappingEntity::getRestStopServiceAreaCode)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public int findActiveChargerCount(String serviceAreaCode) {
        if (!StringUtils.hasText(serviceAreaCode)) {
            return 0;
        }
        Set<String> statIds = mappingRepository.findAllByRestStopServiceAreaCodeIn(Set.of(serviceAreaCode)).stream()
                .map(EvChargerStationMappingEntity::getStatId)
                .collect(Collectors.toSet());
        if (statIds.isEmpty()) {
            return 0;
        }
        return evChargerRepository.findAllByStatIdInAndDelYn(statIds, ACTIVE).size();
    }
}
