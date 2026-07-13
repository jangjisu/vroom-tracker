package com.restroute.service.evcharger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restroute.client.EvChargerApiClient;
import com.restroute.client.response.EvChargerResponse;
import com.restroute.domain.EvChargerEntity;
import com.restroute.repository.EvChargerRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class EvChargerSyncServiceTest {

    @Mock
    private EvChargerApiClient evChargerApiClient;

    @Mock
    private EvChargerRepository evChargerRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    private EvChargerSyncService evChargerSyncService;

    @BeforeEach
    void setUp() {
        evChargerSyncService = new EvChargerSyncService(evChargerApiClient, evChargerRepository, transactionTemplate);
    }

    @Test
    @DisplayName("전체 페이지를 조회하고 C001 고속도로 휴게소 충전기만 upsert한다")
    void refreshEvChargers_fetchesAllPagesAndFiltersHighwayRestStops() throws Exception {
        runTransactionCallback();
        when(evChargerRepository.findAll()).thenReturn(List.of());
        when(evChargerApiClient.getChargerInfo(1)).thenReturn(response(401, 1, charger("ME1", "01", "C001")));
        when(evChargerApiClient.getChargerInfo(2)).thenReturn(response(401, 2, charger("ME2", "01", "C003")));

        int savedCount = evChargerSyncService.refreshEvChargers();

        assertThat(savedCount).isEqualTo(1);
        assertThat(captureSavedEntities())
                .extracting(EvChargerEntity::getStatId)
                .containsExactly("ME1");
        verify(evChargerApiClient).getChargerInfo(2);
    }

    @Test
    @DisplayName("같은 statId와 chgerId가 있으면 기존 충전기 행을 업데이트한다")
    void refreshEvChargers_updatesExistingNaturalKey() throws Exception {
        runTransactionCallback();
        EvChargerEntity existing = chargerEntity("ME1", "01", "C001", "existing");
        when(evChargerRepository.findAll()).thenReturn(List.of(existing));
        when(evChargerApiClient.getChargerInfo(1)).thenReturn(response(1, 1, charger("ME1", "01", "C001", "updated")));

        evChargerSyncService.refreshEvChargers();

        assertThat(captureSavedEntities()).singleElement().satisfies(entity -> {
            assertThat(entity).isSameAs(existing);
            assertThat(entity.getStatNm()).isEqualTo("updated");
        });
    }

    @Test
    @DisplayName("중간 페이지 실패 시 기존 데이터를 조회하거나 저장하지 않는다")
    void refreshEvChargers_keepsExistingDataWhenPageFails() throws Exception {
        when(evChargerApiClient.getChargerInfo(1)).thenReturn(response(401, 1, charger("ME1", "01", "C001")));
        RuntimeException exception = new RuntimeException("timeout");
        when(evChargerApiClient.getChargerInfo(2)).thenThrow(exception);

        assertThatThrownBy(() -> evChargerSyncService.refreshEvChargers()).isSameAs(exception);

        verify(evChargerRepository, never()).findAll();
        verify(evChargerRepository, never()).saveAll(any());
    }

    private EvChargerResponse response(int totalCount, int pageNo, String... items) throws Exception {
        String json = "{\"resultCode\":\"00\",\"resultMsg\":\"NORMAL SERVICE.\",\"totalCount\":"
                + totalCount
                + ",\"pageNo\":"
                + pageNo
                + ",\"numOfRows\":400,\"items\":{\"item\":["
                + String.join(",", items)
                + "]}}";
        return new ObjectMapper().readValue(json, EvChargerResponse.class);
    }

    private String charger(String statId, String chgerId, String kindDetail) {
        return charger(statId, chgerId, kindDetail, "station");
    }

    private String charger(String statId, String chgerId, String kindDetail, String statName) {
        return "{\"statNm\":\""
                + statName
                + "\",\"statId\":\""
                + statId
                + "\",\"chgerId\":\""
                + chgerId
                + "\",\"kindDetail\":\""
                + kindDetail
                + "\",\"delYn\":\"N\"}";
    }

    private EvChargerEntity chargerEntity(String statId, String chgerId, String kindDetail, String statName)
            throws Exception {
        return EvChargerEntity.from(new ObjectMapper()
                .readValue(
                        charger(statId, chgerId, kindDetail, statName),
                        com.restroute.client.response.EvChargerItem.class));
    }

    private void runTransactionCallback() {
        doAnswer(invocation -> {
                    Consumer<TransactionStatus> action = invocation.getArgument(0);
                    action.accept(org.mockito.Mockito.mock(TransactionStatus.class));
                    return null;
                })
                .when(transactionTemplate)
                .executeWithoutResult(any());
    }

    @SuppressWarnings("unchecked")
    private List<EvChargerEntity> captureSavedEntities() {
        ArgumentCaptor<Iterable<EvChargerEntity>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(evChargerRepository).saveAll(captor.capture());
        List<EvChargerEntity> entities = new ArrayList<>();
        captor.getValue().forEach(entities::add);
        return entities;
    }
}
