package com.vroomtracker.service;

import static com.vroomtracker.support.RestStopTestFixtures.restOilItem;
import static com.vroomtracker.support.RestStopTestFixtures.restOilResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vroomtracker.client.ExApiClient;
import com.vroomtracker.client.ExApiException;
import com.vroomtracker.client.response.RestOilItem;
import com.vroomtracker.domain.RestOilEntity;
import com.vroomtracker.repository.RestOilRepository;
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
class RestOilSyncServiceTest {

    @Mock
    private ExApiClient exApiClient;

    @Mock
    private RestOilRepository restOilRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    private RestOilSyncService restOilSyncService;

    @BeforeEach
    void setUp() {
        restOilSyncService = new RestOilSyncService(exApiClient, restOilRepository, transactionTemplate);
    }

    @Test
    @DisplayName("테이블이 비어 있으면 주유소 편의시설을 초기 적재한다")
    void initializeRestOilsIfEmpty_refreshesWhenTableIsEmpty() {
        runTransactionCallback();
        RestOilItem item = restOilItem("000002", "서울만남(부산)주유소");
        when(restOilRepository.count()).thenReturn(0L);
        when(exApiClient.getRestOilList()).thenReturn(restOilResponse("SUCCESS", List.of(item)));

        int savedCount = restOilSyncService.initializeRestOilsIfEmpty();

        assertThat(savedCount).isEqualTo(1);
        verify(restOilRepository).deleteAllInBatch();
    }

    @Test
    @DisplayName("테이블에 데이터가 있으면 초기 적재를 생략한다")
    void initializeRestOilsIfEmpty_skipsWhenTableHasData() {
        when(restOilRepository.count()).thenReturn(1L);

        int savedCount = restOilSyncService.initializeRestOilsIfEmpty();

        assertThat(savedCount).isZero();
        verify(exApiClient, never()).getRestOilList();
    }

    @Test
    @DisplayName("주유소 편의시설 API 전체 목록을 복수 행 그대로 교체 저장한다")
    void refreshRestOils_fetchesAndReplacesRows() {
        runTransactionCallback();
        RestOilItem first = restOilItem("000002", "서울만남(부산)주유소");
        RestOilItem second = restOilItem("000002", "서울만남(부산)주유소");
        when(exApiClient.getRestOilList()).thenReturn(restOilResponse("SUCCESS", List.of(first, second)));

        int savedCount = restOilSyncService.refreshRestOils();

        assertThat(savedCount).isEqualTo(2);
        List<RestOilEntity> savedEntities = captureSavedEntities();
        assertThat(savedEntities)
                .extracting(RestOilEntity::getNormalizedStationName)
                .containsExactly("서울만남(부산)", "서울만남(부산)");
    }

    @Test
    @DisplayName("주유소 편의시설 API 호출이 실패하면 기존 DB를 교체하지 않는다")
    void refreshRestOils_doesNotReplaceRowsWhenApiFails() {
        ExApiException exception =
                new ExApiException("https://data.ex.co.kr/openapi/restinfo/restOilList?key=test-key", "failed");
        when(exApiClient.getRestOilList()).thenThrow(exception);

        assertThatThrownBy(restOilSyncService::refreshRestOils).isSameAs(exception);

        verify(restOilRepository, never()).deleteAllInBatch();
        verify(restOilRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("API list가 null이면 빈 목록으로 전체 교체한다")
    void refreshRestOils_replacesWithEmptyRowsWhenListIsNull() {
        runTransactionCallback();
        var response = restOilResponse("SUCCESS", List.of());
        ReflectionTestUtils.setField(response, "list", null);
        when(exApiClient.getRestOilList()).thenReturn(response);

        int savedCount = restOilSyncService.refreshRestOils();

        assertThat(savedCount).isZero();
        verify(restOilRepository).deleteAllInBatch();
        verify(restOilRepository).saveAll(List.of());
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
    private List<RestOilEntity> captureSavedEntities() {
        ArgumentCaptor<Iterable<RestOilEntity>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(restOilRepository).saveAll(captor.capture());

        List<RestOilEntity> entities = new ArrayList<>();
        captor.getValue().forEach(entities::add);
        return entities;
    }
}
