package com.restroute.service.evcharger;

import static com.restroute.support.RestStopTestFixtures.restStopItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restroute.client.response.EvChargerItem;
import com.restroute.domain.EvChargerEntity;
import com.restroute.domain.EvChargerStationMappingEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.EvChargerRepository;
import com.restroute.repository.EvChargerStationMappingRepository;
import com.restroute.repository.RestStopRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvChargerRestStopBackfillServiceTest {

    @Mock
    private EvChargerRepository evChargerRepository;

    @Mock
    private EvChargerStationMappingRepository mappingRepository;

    @Mock
    private RestStopRepository restStopRepository;

    private EvChargerRestStopBackfillService backfillService;

    @BeforeEach
    void setUp() {
        backfillService =
                new EvChargerRestStopBackfillService(evChargerRepository, mappingRepository, restStopRepository);
        lenient().when(mappingRepository.findAllByStatIdMap()).thenReturn(Map.of());
    }

    @Test
    @DisplayName("충전소 좌표와 정규화된 이름이 일치하면 휴게소 serviceAreaCode로 매핑한다")
    void backfill_matchesStationByCoordinateAndName() throws Exception {
        EvChargerEntity station = chargerEntity("ME178009", "01", "서울만남(부산) 휴게소", "37.4600218", "127.0420378");
        when(evChargerRepository.findAll()).thenReturn(List.of(station));
        when(restStopRepository.findAll()).thenReturn(List.of(restStop("서울만남(부산)휴게소", "A00001")));

        Map<String, Integer> result = backfillService.backfill();

        assertThat(result).containsEntry("matchedCount", 1).containsEntry("unmatchedCount", 0);
        EvChargerStationMappingEntity mapping = captureMappings().get(0);
        assertThat(mapping.getRestStopServiceAreaCode()).isEqualTo("A00001");
        assertThat(mapping.getDistanceMeters()).isLessThan(100.0);
        assertThat(mapping.getMatchType()).isEqualTo("COORDINATE_AND_NAME");
    }

    @Test
    @DisplayName("같은 statId의 여러 충전기는 한 번만 휴게소 매핑을 생성한다")
    void backfill_deduplicatesChargersByStationId() throws Exception {
        EvChargerEntity first = chargerEntity("ME1", "01", "A휴게소", "37.4599", "127.0425");
        EvChargerEntity second = chargerEntity("ME1", "02", "A휴게소", "37.4599", "127.0425");
        when(evChargerRepository.findAll()).thenReturn(List.of(first, second));
        when(restStopRepository.findAll()).thenReturn(List.of(restStop("A휴게소", "A00001")));

        backfillService.backfill();

        assertThat(captureMappings()).hasSize(1);
        verify(mappingRepository).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("좌표 후보가 여러 개이고 이름으로 구분할 수 없으면 미매칭으로 보존한다")
    void backfill_keepsAmbiguousStationUnmatched() throws Exception {
        EvChargerEntity station = chargerEntity("ME2", "01", "다른충전소", "37.4599", "127.0425");
        when(evChargerRepository.findAll()).thenReturn(List.of(station));
        when(restStopRepository.findAll()).thenReturn(List.of(restStop("A휴게소", "A00001"), restStop("B휴게소", "B00001")));

        Map<String, Integer> result = backfillService.backfill();

        assertThat(result).containsEntry("matchedCount", 0).containsEntry("unmatchedCount", 1);
        EvChargerStationMappingEntity mapping = captureMappings().get(0);
        assertThat(mapping.getRestStopServiceAreaCode()).isNull();
        assertThat(mapping.getMatchType()).isEqualTo("AMBIGUOUS");
    }

    @Test
    @DisplayName("휴게소명 정규화는 공백과 휴게소 접미사를 제거하고 방향 괄호는 유지한다")
    void normalizedName_removesOnlyFormatting() {
        assertThat(EvChargerRestStopBackfillService.normalizedName("서울만남(부산) 휴게소"))
                .isEqualTo("서울만남(부산)");
    }

    private RestStopEntity restStop(String name, String serviceAreaCode) {
        return RestStopEntity.from(restStopItem("001", name, serviceAreaCode));
    }

    private EvChargerEntity chargerEntity(
            String statId, String chgerId, String statName, String latitude, String longitude) throws Exception {
        String json = "{\"statNm\":\""
                + statName
                + "\",\"statId\":\""
                + statId
                + "\",\"chgerId\":\""
                + chgerId
                + "\",\"lat\":\""
                + latitude
                + "\",\"lng\":\""
                + longitude
                + "\",\"delYn\":\"N\"}";
        EvChargerItem item = new ObjectMapper().readValue(json, EvChargerItem.class);
        return EvChargerEntity.from(item);
    }

    @SuppressWarnings("unchecked")
    private List<EvChargerStationMappingEntity> captureMappings() {
        ArgumentCaptor<Iterable<EvChargerStationMappingEntity>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(mappingRepository).saveAll(captor.capture());
        List<EvChargerStationMappingEntity> mappings = new ArrayList<>();
        captor.getValue().forEach(mappings::add);
        return mappings;
    }
}
