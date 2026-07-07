package com.restroute.service;

import static com.restroute.support.RestStopTestFixtures.highwayServiceAreaInfoItem;
import static com.restroute.support.RestStopTestFixtures.highwayServiceAreaInfoResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restroute.client.ExApiClient;
import com.restroute.client.ExApiException;
import com.restroute.client.response.HighwayServiceAreaInfoItem;
import com.restroute.client.response.HighwayServiceAreaInfoResponse;
import com.restroute.domain.HighwayServiceAreaInfoEntity;
import com.restroute.repository.HighwayServiceAreaInfoRepository;
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
class HighwayServiceAreaInfoSyncServiceTest {

    @Mock
    private ExApiClient exApiClient;

    @Mock
    private HighwayServiceAreaInfoRepository highwayServiceAreaInfoRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    private HighwayServiceAreaInfoSyncService highwayServiceAreaInfoSyncService;

    @BeforeEach
    void setUp() {
        highwayServiceAreaInfoSyncService = new HighwayServiceAreaInfoSyncService(
                exApiClient, highwayServiceAreaInfoRepository, transactionTemplate);
    }

    @Test
    @DisplayName("고속도로 휴게소 정보 API를 호출하고 전체 목록을 교체 저장한다")
    void refreshHighwayServiceAreaInfos_fetchesAndReplacesRows() {
        runTransactionCallback();
        HighwayServiceAreaInfoItem first = highwayServiceAreaInfoItem("000561", "북대전(논산)졸음쉼터");
        HighwayServiceAreaInfoItem second = highwayServiceAreaInfoItem("000616", "김제(새만금)주유소");
        when(exApiClient.getHighwayServiceAreaInfoList())
                .thenReturn(highwayServiceAreaInfoResponse("SUCCESS", List.of(first, second)));

        int savedCount = highwayServiceAreaInfoSyncService.refreshHighwayServiceAreaInfos();

        assertThat(savedCount).isEqualTo(2);
        verify(highwayServiceAreaInfoRepository).deleteAllInBatch();
        List<HighwayServiceAreaInfoEntity> savedEntities = captureSavedEntities();
        assertThat(savedEntities)
                .extracting(HighwayServiceAreaInfoEntity::getServiceAreaCode)
                .containsExactly("000561", "000616");
    }

    @Test
    @DisplayName("고속도로 휴게소 정보 API 호출이 실패하면 기존 DB를 교체하지 않는다")
    void refreshHighwayServiceAreaInfos_doesNotReplaceRowsWhenApiFails() {
        ExApiException exception =
                new ExApiException("https://data.ex.co.kr/openapi/restinfo/hiwaySvarInfoList?key=<redacted>", "failed");
        when(exApiClient.getHighwayServiceAreaInfoList()).thenThrow(exception);

        assertThatThrownBy(() -> highwayServiceAreaInfoSyncService.refreshHighwayServiceAreaInfos())
                .isSameAs(exception);

        verify(highwayServiceAreaInfoRepository, never()).deleteAllInBatch();
        verify(highwayServiceAreaInfoRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("고속도로 휴게소 정보 API 성공 여부는 Client 계약을 신뢰하고 다시 검사하지 않는다")
    void refreshHighwayServiceAreaInfos_doesNotCheckApiSuccessAgain() {
        runTransactionCallback();
        HighwayServiceAreaInfoResponse response = mock(HighwayServiceAreaInfoResponse.class);
        when(response.getList()).thenReturn(List.of());
        when(exApiClient.getHighwayServiceAreaInfoList()).thenReturn(response);

        highwayServiceAreaInfoSyncService.refreshHighwayServiceAreaInfos();

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
    private List<HighwayServiceAreaInfoEntity> captureSavedEntities() {
        ArgumentCaptor<Iterable<HighwayServiceAreaInfoEntity>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(highwayServiceAreaInfoRepository).saveAll(captor.capture());

        List<HighwayServiceAreaInfoEntity> entities = new ArrayList<>();
        captor.getValue().forEach(entities::add);
        return entities;
    }
}
