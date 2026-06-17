package com.vroomtracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vroomtracker.client.ExApiClient;
import com.vroomtracker.client.ExApiException;
import com.vroomtracker.client.response.RestBestfoodResponse;
import com.vroomtracker.domain.RestFoodEntity;
import com.vroomtracker.repository.RestFoodRepository;
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
class RestFoodSyncServiceTest {

    @Mock
    private ExApiClient exApiClient;

    @Mock
    private RestFoodRepository restFoodRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    private RestFoodSyncService restFoodSyncService;

    @BeforeEach
    void setUp() {
        restFoodSyncService = new RestFoodSyncService(exApiClient, restFoodRepository, transactionTemplate);
    }

    @Test
    @DisplayName("테이블이 비어 있으면 음식 메뉴를 초기 적재한다")
    void initializeRestFoodsIfEmpty_refreshesWhenTableIsEmpty() throws Exception {
        runTransactionCallback();
        when(restFoodRepository.count()).thenReturn(0L);
        when(exApiClient.getRestBestfoodList(1)).thenReturn(foodResponse(1, "농심어묵우동"));

        int savedCount = restFoodSyncService.initializeRestFoodsIfEmpty();

        assertThat(savedCount).isEqualTo(1);
        verify(restFoodRepository).deleteAllInBatch();
    }

    @Test
    @DisplayName("테이블에 데이터가 있으면 음식 메뉴 초기 적재를 생략한다")
    void initializeRestFoodsIfEmpty_skipsWhenTableHasData() {
        when(restFoodRepository.count()).thenReturn(1L);

        int savedCount = restFoodSyncService.initializeRestFoodsIfEmpty();

        assertThat(savedCount).isZero();
        verify(exApiClient, never()).getRestBestfoodList(1);
    }

    @Test
    @DisplayName("pageSize만큼 페이지를 순회해 전체 교체 저장한다")
    void refreshRestFoods_fetchesAllPagesAndReplacesRows() throws Exception {
        runTransactionCallback();
        when(exApiClient.getRestBestfoodList(1)).thenReturn(foodResponse(2, "농심어묵우동"));
        when(exApiClient.getRestBestfoodList(2)).thenReturn(foodResponse(2, "한우국밥"));

        int savedCount = restFoodSyncService.refreshRestFoods();

        assertThat(savedCount).isEqualTo(2);
        verify(exApiClient).getRestBestfoodList(1);
        verify(exApiClient).getRestBestfoodList(2);
        assertThat(captureSavedEntities())
                .extracting(RestFoodEntity::getFoodName)
                .containsExactly("농심어묵우동", "한우국밥");
    }

    @Test
    @DisplayName("음식 메뉴 API 호출이 실패하면 기존 DB를 교체하지 않는다")
    void refreshRestFoods_doesNotReplaceRowsWhenApiFails() throws Exception {
        ExApiException exception =
                new ExApiException("https://data.ex.co.kr/openapi/restinfo/restBestfoodList?key=test-key", "failed");
        when(exApiClient.getRestBestfoodList(1)).thenReturn(foodResponse(2));
        when(exApiClient.getRestBestfoodList(2)).thenThrow(exception);

        assertThatThrownBy(restFoodSyncService::refreshRestFoods).isSameAs(exception);

        verify(restFoodRepository, never()).deleteAllInBatch();
        verify(restFoodRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("API list가 null이면 빈 목록으로 전체 교체한다")
    void refreshRestFoods_replacesWithEmptyRowsWhenListIsNull() throws Exception {
        runTransactionCallback();
        RestBestfoodResponse response = foodResponse(1);
        ReflectionTestUtils.setField(response, "list", null);
        when(exApiClient.getRestBestfoodList(1)).thenReturn(response);

        int savedCount = restFoodSyncService.refreshRestFoods();

        assertThat(savedCount).isZero();
        verify(restFoodRepository).deleteAllInBatch();
        verify(restFoodRepository).saveAll(List.of());
    }

    private RestBestfoodResponse foodResponse(int pageSize, String... foodNames) throws Exception {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < foodNames.length; i++) {
            if (i > 0) {
                list.append(",");
            }
            list.append("{\"stdRestCd\":\"000001\",\"foodNm\":\"")
                    .append(foodNames[i])
                    .append("\"}");
        }
        String json = "{\"code\":\"SUCCESS\",\"pageSize\":" + pageSize + ",\"list\":[" + list + "]}";
        return new ObjectMapper().readValue(json, RestBestfoodResponse.class);
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
    private List<RestFoodEntity> captureSavedEntities() {
        ArgumentCaptor<Iterable<RestFoodEntity>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(restFoodRepository).saveAll(captor.capture());

        List<RestFoodEntity> entities = new ArrayList<>();
        captor.getValue().forEach(entities::add);
        return entities;
    }
}
