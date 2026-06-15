package com.vroomtracker.service;

import static com.vroomtracker.support.RestStopTestFixtures.restOilPriceItem;
import static com.vroomtracker.support.RestStopTestFixtures.restOilPriceResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vroomtracker.client.ExApiClient;
import com.vroomtracker.client.ExApiException;
import com.vroomtracker.client.response.RestOilPriceItem;
import com.vroomtracker.domain.RestOilPriceEntity;
import com.vroomtracker.repository.RestOilPriceRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
class RestOilPriceSyncServiceTest {

    @Mock
    private ExApiClient exApiClient;

    @Mock
    private RestOilPriceRepository restOilPriceRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-15T22:30:00Z"), ZoneId.of("Asia/Seoul"));

    private RestOilPriceSyncService restOilPriceSyncService;

    @BeforeEach
    void setUp() {
        restOilPriceSyncService =
                new RestOilPriceSyncService(exApiClient, restOilPriceRepository, transactionTemplate, clock);
    }

    @Test
    @DisplayName("테이블이 비어 있으면 주유소 가격을 초기 적재한다")
    void initializeRestOilPricesIfEmpty_refreshesWhenTableIsEmpty() {
        runTransactionCallback();
        RestOilPriceItem item = restOilPriceItem("000002", "서울만남(부산)주유소");
        when(restOilPriceRepository.count()).thenReturn(0L);
        when(exApiClient.getCurStateStation(1)).thenReturn(restOilPriceResponse("SUCCESS", List.of(item)));
        when(exApiClient.getCurStateStation(2)).thenReturn(restOilPriceResponse("SUCCESS", List.of()));
        when(exApiClient.getCurStateStation(3)).thenReturn(restOilPriceResponse("SUCCESS", List.of()));

        int savedCount = restOilPriceSyncService.initializeRestOilPricesIfEmpty();

        assertThat(savedCount).isEqualTo(1);
        verify(restOilPriceRepository).deleteAllInBatch();
    }

    @Test
    @DisplayName("테이블에 데이터가 있으면 주유소 가격 초기 적재를 생략한다")
    void initializeRestOilPricesIfEmpty_skipsWhenTableHasData() {
        when(restOilPriceRepository.count()).thenReturn(1L);

        int savedCount = restOilPriceSyncService.initializeRestOilPricesIfEmpty();

        assertThat(savedCount).isZero();
        verify(exApiClient, never()).getCurStateStation(1);
    }

    @Test
    @DisplayName("주유소 가격 API 1~3페이지를 99개씩 조회해 전체 교체 저장한다")
    void refreshRestOilPrices_fetchesThreePagesAndReplacesRows() {
        runTransactionCallback();
        RestOilPriceItem first = restOilPriceItem("000002", "서울만남(부산)주유소");
        RestOilPriceItem second = restOilPriceItem("000006", "기흥(부산)주유소");
        RestOilPriceItem third = restOilPriceItem("000010", "안성(부산)주유소");
        when(exApiClient.getCurStateStation(1)).thenReturn(restOilPriceResponse("SUCCESS", List.of(first)));
        when(exApiClient.getCurStateStation(2)).thenReturn(restOilPriceResponse("SUCCESS", List.of(second)));
        when(exApiClient.getCurStateStation(3)).thenReturn(restOilPriceResponse("SUCCESS", List.of(third)));

        int savedCount = restOilPriceSyncService.refreshRestOilPrices();

        assertThat(savedCount).isEqualTo(3);
        verify(exApiClient).getCurStateStation(1);
        verify(exApiClient).getCurStateStation(2);
        verify(exApiClient).getCurStateStation(3);
        List<RestOilPriceEntity> savedEntities = captureSavedEntities();
        assertThat(savedEntities)
                .extracting(RestOilPriceEntity::getServiceAreaCode2)
                .containsExactly("000002", "000006", "000010");
        assertThat(savedEntities)
                .extracting(RestOilPriceEntity::getLastRefreshedAt)
                .containsOnly(LocalDateTime.of(2026, 6, 16, 7, 30));
    }

    @Test
    @DisplayName("주유소 가격 API 호출이 실패하면 기존 DB를 교체하지 않는다")
    void refreshRestOilPrices_doesNotReplaceRowsWhenApiFails() {
        ExApiException exception =
                new ExApiException("https://data.ex.co.kr/openapi/business/curStateStation?key=test-key", "failed");
        when(exApiClient.getCurStateStation(1)).thenReturn(restOilPriceResponse("SUCCESS", List.of()));
        when(exApiClient.getCurStateStation(2)).thenThrow(exception);

        assertThatThrownBy(restOilPriceSyncService::refreshRestOilPrices).isSameAs(exception);

        verify(restOilPriceRepository, never()).deleteAllInBatch();
        verify(restOilPriceRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("API list가 null이면 빈 목록으로 전체 교체한다")
    void refreshRestOilPrices_replacesWithEmptyRowsWhenListIsNull() {
        runTransactionCallback();
        var response = restOilPriceResponse("SUCCESS", List.of());
        ReflectionTestUtils.setField(response, "list", null);
        when(exApiClient.getCurStateStation(1)).thenReturn(response);
        when(exApiClient.getCurStateStation(2)).thenReturn(restOilPriceResponse("SUCCESS", List.of()));
        when(exApiClient.getCurStateStation(3)).thenReturn(restOilPriceResponse("SUCCESS", List.of()));

        int savedCount = restOilPriceSyncService.refreshRestOilPrices();

        assertThat(savedCount).isZero();
        verify(restOilPriceRepository).deleteAllInBatch();
        verify(restOilPriceRepository).saveAll(List.of());
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
    private List<RestOilPriceEntity> captureSavedEntities() {
        ArgumentCaptor<Iterable<RestOilPriceEntity>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(restOilPriceRepository).saveAll(captor.capture());

        List<RestOilPriceEntity> entities = new ArrayList<>();
        captor.getValue().forEach(entities::add);
        return entities;
    }
}
