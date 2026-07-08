package com.restroute.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restroute.client.ExApiClient;
import com.restroute.client.exception.ExApiException;
import com.restroute.client.response.RestBestfoodItem;
import com.restroute.client.response.RestBestfoodResponse;
import com.restroute.domain.RestFoodEntity;
import com.restroute.repository.RestFoodRepository;
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
        when(restFoodRepository.findAll()).thenReturn(List.of());
        when(exApiClient.getRestBestfoodList(1)).thenReturn(foodResponse(1, "농심어묵우동"));

        int savedCount = restFoodSyncService.initializeRestFoodsIfEmpty();

        assertThat(savedCount).isEqualTo(1);
        assertThat(captureSavedEntities()).extracting(RestFoodEntity::getFoodName).containsExactly("농심어묵우동");
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
    @DisplayName("기존 DB에 없는 자연키(stdRestCd+seq)의 항목은 새로 삽입한다")
    void refreshRestFoods_insertsRowsWithNewNaturalKey() throws Exception {
        runTransactionCallback();
        when(restFoodRepository.findAll()).thenReturn(List.of());
        when(exApiClient.getRestBestfoodList(1)).thenReturn(foodResponse(2, "농심어묵우동", "한우국밥"));
        when(exApiClient.getRestBestfoodList(2)).thenReturn(foodResponse(2, "육개장"));

        int savedCount = restFoodSyncService.refreshRestFoods();

        assertThat(savedCount).isEqualTo(3);
        verify(exApiClient).getRestBestfoodList(1);
        verify(exApiClient).getRestBestfoodList(2);
        assertThat(captureSavedEntities())
                .extracting(RestFoodEntity::getFoodName)
                .containsExactly("농심어묵우동", "한우국밥", "육개장");
    }

    @Test
    @DisplayName("기존 DB에 같은 자연키(stdRestCd+seq)가 있으면 같은 행을 업데이트한다")
    void refreshRestFoods_updatesExistingRowWithSameNaturalKey() throws Exception {
        runTransactionCallback();
        RestBestfoodItem originalItem = foodResponse(1, "농심어묵우동").getList().get(0);
        RestFoodEntity existing = RestFoodEntity.from(originalItem);
        when(restFoodRepository.findAll()).thenReturn(List.of(existing));
        when(exApiClient.getRestBestfoodList(1)).thenReturn(foodResponse(1, "한우국밥"));

        int savedCount = restFoodSyncService.refreshRestFoods();

        assertThat(savedCount).isEqualTo(1);
        List<RestFoodEntity> saved = captureSavedEntities();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0)).isSameAs(existing);
        assertThat(saved.get(0).getFoodName()).isEqualTo("한우국밥");
    }

    @Test
    @DisplayName("음식 메뉴 API 호출이 실패하면 DB를 조회하거나 저장하지 않는다")
    void refreshRestFoods_doesNotUpsertRowsWhenApiFails() throws Exception {
        ExApiException exception =
                new ExApiException("https://data.ex.co.kr/openapi/restinfo/restBestfoodList?key=<redacted>", "failed");
        when(exApiClient.getRestBestfoodList(1)).thenReturn(foodResponse(2));
        when(exApiClient.getRestBestfoodList(2)).thenThrow(exception);

        assertThatThrownBy(restFoodSyncService::refreshRestFoods).isSameAs(exception);

        verify(restFoodRepository, never()).findAll();
        verify(restFoodRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("API list가 null이면 빈 목록으로 upsert해 아무것도 저장하지 않는다")
    void refreshRestFoods_upsertsEmptyListWhenListIsNull() throws Exception {
        runTransactionCallback();
        when(restFoodRepository.findAll()).thenReturn(List.of());
        RestBestfoodResponse response = foodResponse(1);
        ReflectionTestUtils.setField(response, "list", null);
        when(exApiClient.getRestBestfoodList(1)).thenReturn(response);

        int savedCount = restFoodSyncService.refreshRestFoods();

        assertThat(savedCount).isZero();
        verify(restFoodRepository).saveAll(List.of());
    }

    private RestBestfoodResponse foodResponse(int pageSize, String... foodNames) throws Exception {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < foodNames.length; i++) {
            if (i > 0) {
                list.append(",");
            }
            list.append("{\"stdRestCd\":\"000001\",\"seq\":\"")
                    .append(i + 1)
                    .append("\",\"foodNm\":\"")
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
