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
    @DisplayName("매핑된 휴게소 코드를 일괄 조회한다")
    void findMappedServiceAreaCodes_returnsMappedCodes() {
        EvChargerStationMappingEntity mapping = EvChargerStationMappingEntity.of("ME1");
        mapping.updateMatch("A00001");
        when(mappingRepository.findAllByRestStopServiceAreaCodeIn(List.of("A00001")))
                .thenReturn(List.of(mapping));
        List<String> result = queryService.findMappedServiceAreaCodes(List.of("A00001"));

        assertThat(result).containsExactly("A00001");
    }

    @Test
    @DisplayName("휴게소 코드가 없으면 경로용 매핑 결과를 조회하지 않는다")
    void findMappedServiceAreaCodes_returnsEmptyForBlankInput() {
        assertThat(queryService.findMappedServiceAreaCodes(List.of("", " "))).isEmpty();
    }

    @Test
    @DisplayName("매핑된 휴게소의 활성 충전기 대수를 조회한다")
    void findActiveChargerCount_countsActiveChargers() throws Exception {
        EvChargerStationMappingEntity mapping = EvChargerStationMappingEntity.of("ME1");
        mapping.updateMatch("A00001");
        when(mappingRepository.findAllByRestStopServiceAreaCodeIn(List.of("A00001")))
                .thenReturn(List.of(mapping));
        when(evChargerRepository.findAllByStatIdInAndDelYn(List.of("ME1"), "N"))
                .thenReturn(List.of(charger("ME1", "01", "N"), charger("ME1", "02", "N")));

        assertThat(queryService.findActiveChargerCount("A00001")).isEqualTo(2);
    }

    @Test
    @DisplayName("한 휴게소에 여러 statId가 매핑되면 활성 충전기 수를 합산한다")
    void findActiveChargerCount_countsChargersAcrossMultipleStations() throws Exception {
        EvChargerStationMappingEntity firstMapping = EvChargerStationMappingEntity.of("ME1");
        firstMapping.updateMatch("A00001");
        EvChargerStationMappingEntity secondMapping = EvChargerStationMappingEntity.of("ME2");
        secondMapping.updateMatch("A00001");
        when(mappingRepository.findAllByRestStopServiceAreaCodeIn(List.of("A00001")))
                .thenReturn(List.of(firstMapping, secondMapping));
        when(evChargerRepository.findAllByStatIdInAndDelYn(List.of("ME1", "ME2"), "N"))
                .thenReturn(List.of(charger("ME1", "01", "N"), charger("ME1", "02", "N"), charger("ME2", "01", "N")));

        assertThat(queryService.findActiveChargerCount("A00001")).isEqualTo(3);
    }

    @Test
    @DisplayName("매핑이 없거나 휴게소 코드가 없으면 상세 충전기 수를 0으로 반환한다")
    void findActiveChargerCount_returnsZeroWithoutMapping() {
        when(mappingRepository.findAllByRestStopServiceAreaCodeIn(List.of("A00001")))
                .thenReturn(List.of());

        assertThat(queryService.findActiveChargerCount("A00001")).isZero();
        assertThat(queryService.findActiveChargerCount(" ")).isZero();
    }

    private EvChargerEntity charger(String statId, String chgerId, String delYn) throws Exception {
        EvChargerItem item = new ObjectMapper()
                .readValue(
                        "{\"statId\":\"" + statId + "\",\"chgerId\":\"" + chgerId + "\",\"delYn\":\"" + delYn + "\"}",
                        EvChargerItem.class);
        return EvChargerEntity.from(item);
    }
}
