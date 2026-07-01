package com.restroute.service;

import static com.restroute.support.RestStopTestFixtures.restStopDetailItem;
import static com.restroute.support.RestStopTestFixtures.restStopDetailResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restroute.client.ExApiClient;
import com.restroute.client.ExApiException;
import com.restroute.client.response.RestStopDetailItem;
import com.restroute.client.response.RestStopDetailResponse;
import com.restroute.domain.RestStopDetailEntity;
import com.restroute.repository.RestStopDetailRepository;
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
class RestStopDetailSyncServiceTest {

    @Mock
    private ExApiClient exApiClient;

    @Mock
    private RestStopDetailRepository restStopDetailRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    private RestStopDetailSyncService restStopDetailSyncService;

    @BeforeEach
    void setUp() {
        restStopDetailSyncService =
                new RestStopDetailSyncService(exApiClient, restStopDetailRepository, transactionTemplate);
    }

    @Test
    @DisplayName("DB가 비어 있으면 서버 시작 시 휴게소 상세 목록을 적재한다")
    void initializeRestStopDetailsIfEmpty_refreshesWhenTableIsEmpty() {
        when(restStopDetailRepository.count()).thenReturn(0L);
        runTransactionCallback();
        RestStopDetailItem detail = restStopDetailItem("A00078", "건천(부산)휴게소");
        when(exApiClient.getConvenienceServiceArea(1))
                .thenReturn(restStopDetailResponse("SUCCESS", "1", List.of(detail)));

        int savedCount = restStopDetailSyncService.initializeRestStopDetailsIfEmpty();

        assertThat(savedCount).isEqualTo(1);
        verify(restStopDetailRepository).deleteAllInBatch();
        List<RestStopDetailEntity> savedEntities = captureSavedEntities();
        assertThat(savedEntities)
                .extracting(RestStopDetailEntity::getServiceAreaCode)
                .containsExactly("A00078");
    }

    @Test
    @DisplayName("DB에 상세 데이터가 있으면 서버 시작 시 초기 적재를 생략한다")
    void initializeRestStopDetailsIfEmpty_skipsWhenTableHasData() {
        when(restStopDetailRepository.count()).thenReturn(1L);

        int savedCount = restStopDetailSyncService.initializeRestStopDetailsIfEmpty();

        assertThat(savedCount).isZero();
        verify(exApiClient, never()).getConvenienceServiceArea(anyInt());
        verify(restStopDetailRepository, never()).deleteAllInBatch();
        verify(restStopDetailRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("총 페이지 수만큼 휴게소 상세 API를 호출하고 전체 목록을 교체 저장한다")
    void refreshRestStopDetails_fetchesAllPagesAndReplacesRows() {
        runTransactionCallback();
        RestStopDetailItem first = restStopDetailItem("A00078", "건천(부산)휴게소");
        RestStopDetailItem second = restStopDetailItem("A00315", "처인휴게소");
        when(exApiClient.getConvenienceServiceArea(1))
                .thenReturn(restStopDetailResponse("SUCCESS", "2", List.of(first)));
        when(exApiClient.getConvenienceServiceArea(2))
                .thenReturn(restStopDetailResponse("SUCCESS", "2", List.of(second)));

        int savedCount = restStopDetailSyncService.refreshRestStopDetails();

        assertThat(savedCount).isEqualTo(2);
        verify(restStopDetailRepository).deleteAllInBatch();
        List<RestStopDetailEntity> savedEntities = captureSavedEntities();
        assertThat(savedEntities)
                .extracting(RestStopDetailEntity::getServiceAreaCode)
                .containsExactly("A00078", "A00315");
    }

    @Test
    @DisplayName("상세 API 호출이 실패하면 기존 DB를 교체하지 않는다")
    void refreshRestStopDetails_doesNotReplaceRowsWhenApiFails() {
        ExApiException exception =
                new ExApiException("https://data.ex.co.kr/openapi/business/conveniServiceArea?key=test-key", "failed");
        when(exApiClient.getConvenienceServiceArea(1)).thenThrow(exception);

        assertThatThrownBy(() -> restStopDetailSyncService.refreshRestStopDetails())
                .isSameAs(exception);

        verify(restStopDetailRepository, never()).deleteAllInBatch();
        verify(restStopDetailRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("상세 API 응답 성공 여부는 Client 계약을 신뢰하고 다시 검사하지 않는다")
    void refreshRestStopDetails_doesNotCheckApiSuccessAgain() {
        runTransactionCallback();
        RestStopDetailResponse response = mock(RestStopDetailResponse.class);
        when(response.getTotalPageCount()).thenReturn(1);
        when(response.getList()).thenReturn(List.of());
        when(exApiClient.getConvenienceServiceArea(1)).thenReturn(response);

        restStopDetailSyncService.refreshRestStopDetails();

        verify(response, never()).isSuccess();
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
    private List<RestStopDetailEntity> captureSavedEntities() {
        ArgumentCaptor<Iterable<RestStopDetailEntity>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(restStopDetailRepository).saveAll(captor.capture());

        List<RestStopDetailEntity> entities = new ArrayList<>();
        captor.getValue().forEach(entities::add);
        return entities;
    }
}
