package com.restroute.repository;

import com.restroute.domain.EvChargerStationMappingEntity;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvChargerStationMappingRepository extends JpaRepository<EvChargerStationMappingEntity, Long> {

    List<EvChargerStationMappingEntity> findAllByRestStopServiceAreaCodeIn(Collection<String> serviceAreaCodes);

    default Map<String, EvChargerStationMappingEntity> findAllByStatIdMap() {
        return findAll().stream()
                .collect(Collectors.toMap(
                        EvChargerStationMappingEntity::getStatId, Function.identity(), (first, second) -> first));
    }
}
