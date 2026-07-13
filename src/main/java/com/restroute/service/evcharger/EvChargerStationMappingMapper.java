package com.restroute.service.evcharger;

import com.restroute.domain.EvChargerEntity;
import com.restroute.domain.EvChargerStationMappingEntity;
import com.restroute.domain.RestStopEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EvChargerStationMappingMapper {

    public List<EvChargerStationMappingEntity> map(
            List<RestStopEntity> restStops, List<EvChargerEntity> evChargerStations) {
        return List.of();
    }
}
