package com.vroomtracker.service;

import static com.vroomtracker.support.RestStopTestFixtures.restStopItem;
import static com.vroomtracker.support.RestStopTestFixtures.restStopResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vroomtracker.client.ExApiClient;
import com.vroomtracker.client.response.RestStopItem;
import com.vroomtracker.domain.RestStopEntity;
import com.vroomtracker.repository.RestStopRepository;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class RestStopSyncServiceTest {

    @Mock
    private ExApiClient exApiClient;

    @Mock
    private RestStopRepository restStopRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    private RestStopSyncService restStopSyncService;

    @BeforeEach
    void setUp() {
        restStopSyncService = new RestStopSyncService(exApiClient, restStopRepository, transactionTemplate);
        ReflectionTestUtils.setField(restStopSyncService, "apiKey", "test-key");
    }

    @Test
    @DisplayName("pageSize만큼 휴게소 API를 호출하고 전체 목록을 교체 저장한다")
    void refreshRestStops_fetchesAllPagesAndReplacesRows() {
        runTransactionCallback();
        RestStopItem first = restStopItem("001", "서울만남(부산)휴게소");
        RestStopItem second = restStopItem("002", "죽전(서울)휴게소");
        RestStopItem third = restStopItem("003", "기흥(부산)휴게소");
        when(exApiClient.getLocationInfoRest("test-key", "json", "99", "1"))
                .thenReturn(restStopResponse("SUCCESS", "3", List.of(first)));
        when(exApiClient.getLocationInfoRest("test-key", "json", "99", "2"))
                .thenReturn(restStopResponse("SUCCESS", "3", List.of(second)));
        when(exApiClient.getLocationInfoRest("test-key", "json", "99", "3"))
                .thenReturn(restStopResponse("SUCCESS", "3", List.of(third)));

        int savedCount = restStopSyncService.refreshRestStops();

        assertThat(savedCount).isEqualTo(3);
        verify(restStopRepository).deleteAllInBatch();
        List<RestStopEntity> savedEntities = captureSavedEntities();
        assertThat(savedEntities).extracting(RestStopEntity::getUnitCode).containsExactly("001", "002", "003");
    }

    @Test
    @DisplayName("DB가 비어 있으면 서버 시작 시 휴게소 목록을 적재한다")
    void initializeRestStopsIfEmpty_refreshesWhenTableIsEmpty() {
        when(restStopRepository.count()).thenReturn(0L);
        runTransactionCallback();
        RestStopItem restStop = restStopItem("001", "서울만남(부산)휴게소");
        when(exApiClient.getLocationInfoRest("test-key", "json", "99", "1"))
                .thenReturn(restStopResponse("SUCCESS", "1", List.of(restStop)));

        int savedCount = restStopSyncService.initializeRestStopsIfEmpty();

        assertThat(savedCount).isEqualTo(1);
        verify(restStopRepository).deleteAllInBatch();
        List<RestStopEntity> savedEntities = captureSavedEntities();
        assertThat(savedEntities).extracting(RestStopEntity::getUnitCode).containsExactly("001");
    }

    @Test
    @DisplayName("DB에 데이터가 있으면 서버 시작 시 초기 적재를 생략한다")
    void initializeRestStopsIfEmpty_skipsWhenTableHasData() {
        when(restStopRepository.count()).thenReturn(1L);

        int savedCount = restStopSyncService.initializeRestStopsIfEmpty();

        assertThat(savedCount).isZero();
        verify(exApiClient, never()).getLocationInfoRest(any(), any(), any(), any());
        verify(restStopRepository, never()).deleteAllInBatch();
        verify(restStopRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("API 응답이 실패하면 기존 DB를 교체하지 않는다")
    void refreshRestStops_doesNotReplaceRowsWhenApiFails() {
        when(exApiClient.getLocationInfoRest("test-key", "json", "99", "1"))
                .thenReturn(restStopResponse("ERROR", "1", List.of()));

        assertThatThrownBy(() -> restStopSyncService.refreshRestStops())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to fetch rest stop page: 1");

        verify(restStopRepository, never()).deleteAllInBatch();
        verify(restStopRepository, never()).saveAll(any());
    }

    private void runTransactionCallback() {
        doAnswer(invocation -> {
                    Consumer<TransactionStatus> action = invocation.getArgument(0);
                    action.accept(mock(TransactionStatus.class));
                    return null;
                })
                .when(transactionTemplate)
                .executeWithoutResult(any());
    }

    @SuppressWarnings("unchecked")
    private List<RestStopEntity> captureSavedEntities() {
        ArgumentCaptor<Iterable<RestStopEntity>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(restStopRepository).saveAll(captor.capture());

        List<RestStopEntity> entities = new ArrayList<>();
        captor.getValue().forEach(entities::add);
        return entities;
    }
}
