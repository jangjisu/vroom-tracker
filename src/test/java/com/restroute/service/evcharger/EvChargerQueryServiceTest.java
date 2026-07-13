package com.restroute.service.evcharger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restroute.client.response.EvChargerItem;
import com.restroute.domain.EvChargerEntity;
import com.restroute.domain.EvChargerStationMappingEntity;
import com.restroute.repository.EvChargerRepository;
import com.restroute.repository.EvChargerStationMappingRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvChargerQueryServiceTest {

    @Mock
    private EvChargerRepository evChargerRepository;

    @Mock
    private EvChargerStationMappingRepository mappingRepository;

    private EvChargerQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new EvChargerQueryService(evChargerRepository, mappingRepository);
    }

    @Test
    @DisplayName("매핑된 휴게소별 활성 충전기 대수를 일괄 조회한다")
    void findActiveChargerCounts_groupsActiveChargersByRestStop() throws Exception {
        EvChargerStationMappingEntity mapping = EvChargerStationMappingEntity.of("ME1");
        mapping.updateMatch("A00001", 40.0, "COORDINATE");
        when(mappingRepository.findAllByRestStopServiceAreaCodeIn(Set.of("A00001")))
                .thenReturn(List.of(mapping));
        when(evChargerRepository.findAllByStatIdInAndDelYn(Set.of("ME1"), "N"))
                .thenReturn(List.of(charger("ME1", "01", "N"), charger("ME1", "02", "N")));

        Map<String, Integer> result = queryService.findActiveChargerCounts(List.of("A00001"));

        assertThat(result).containsEntry("A00001", 2);
    }

    @Test
    @DisplayName("매핑되지 않은 휴게소는 충전기 대수를 0으로 반환한다")
    void findActiveChargerCounts_returnsEmptyWhenNoMappingExists() {
        when(mappingRepository.findAllByRestStopServiceAreaCodeIn(Set.of("A00001")))
                .thenReturn(List.of());

        assertThat(queryService.findActiveChargerCounts(List.of("A00001"))).isEmpty();
    }

    private EvChargerEntity charger(String statId, String chgerId, String delYn) throws Exception {
        EvChargerItem item = new ObjectMapper()
                .readValue(
                        "{\"statId\":\"" + statId + "\",\"chgerId\":\"" + chgerId + "\",\"delYn\":\"" + delYn + "\"}",
                        EvChargerItem.class);
        return EvChargerEntity.from(item);
    }
}
