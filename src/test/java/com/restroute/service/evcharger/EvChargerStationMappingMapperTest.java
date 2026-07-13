package com.restroute.service.evcharger;

import static org.assertj.core.api.Assertions.assertThat;

import com.restroute.domain.EvChargerEntity;
import com.restroute.domain.RestStopEntity;
import java.util.List;
import org.junit.jupiter.api.Test;

class EvChargerStationMappingMapperTest {

    @Test
    void map_returnsEmptyMappingsUntilMatchingRulesAreImplemented() {
        EvChargerStationMappingMapper mapper = new EvChargerStationMappingMapper();

        List<?> result = mapper.map(List.<RestStopEntity>of(), List.<EvChargerEntity>of());

        assertThat(result).isEmpty();
    }
}
