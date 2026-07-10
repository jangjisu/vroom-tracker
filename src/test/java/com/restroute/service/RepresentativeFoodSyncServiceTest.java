package com.restroute.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restroute.client.ExApiClient;
import com.restroute.client.response.RepresentativeFoodItem;
import com.restroute.client.response.RepresentativeFoodResponse;
import com.restroute.domain.RepresentativeFoodEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.RepresentativeFoodRepository;
import com.restroute.repository.RestStopRepository;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class RepresentativeFoodSyncServiceTest {

    @Mock
    private ExApiClient exApiClient;

    @Mock
    private RepresentativeFoodRepository representativeFoodRepository;

    @Mock
    private RestStopRepository restStopRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private RestStopEntity restStop;

    private RepresentativeFoodSyncService service;

    @BeforeEach
    void setUp() {
        service = new RepresentativeFoodSyncService(
                exApiClient, representativeFoodRepository, restStopRepository, transactionTemplate);
        lenient().when(restStopRepository.findAll()).thenReturn(List.of(restStop));
        lenient().when(restStop.getServiceAreaCode()).thenReturn("A00001");
        lenient()
                .doAnswer(invocation -> {
                    Consumer<?> callback = invocation.getArgument(0);
                    callback.accept(null);
                    return null;
                })
                .when(transactionTemplate)
                .executeWithoutResult(any());
    }

    @Test
    @DisplayName("전체 페이지를 조회하고 직접 매칭된 키와 비매칭 원본을 함께 저장한다")
    void refresh_fetchesAllPagesAndStoresMatchKey() {
        RepresentativeFoodItem matched = item("A00001", "서울만남", "말죽거리국밥", "￦6,000");
        RepresentativeFoodItem unmatched = item("A99999", "없는휴게소", "대표메뉴", "￦7,000");
        when(exApiClient.getRepresentativeFoodServiceArea(1)).thenReturn(response(2, matched));
        when(exApiClient.getRepresentativeFoodServiceArea(2)).thenReturn(response(2, unmatched));

        int savedCount = service.refreshRepresentativeFoods();

        assertThat(savedCount).isEqualTo(2);
        ArgumentCaptor<List<RepresentativeFoodEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(representativeFoodRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(captor.getValue().get(0).getRestStopServiceAreaCode()).isEqualTo("A00001");
        assertThat(captor.getValue().get(1).getRestStopServiceAreaCode()).isNull();
        verify(representativeFoodRepository).deleteAllInBatch();
    }

    @Test
    @DisplayName("중간 페이지가 실패하면 기존 대표 음식 데이터를 삭제하지 않는다")
    void refresh_preservesExistingDataWhenPageFails() {
        when(exApiClient.getRepresentativeFoodServiceArea(1))
                .thenReturn(response(3, item("A00001", "휴게소", "메뉴", "가격")));
        when(exApiClient.getRepresentativeFoodServiceArea(2)).thenThrow(new IllegalStateException("page 2 failed"));

        assertThatThrownBy(() -> service.refreshRepresentativeFoods())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("page 2 failed");

        verify(representativeFoodRepository, never()).deleteAllInBatch();
        verify(representativeFoodRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("대표 음식 테이블에 데이터가 있으면 시작 초기 동기화를 건너뛴다")
    void initialize_skipsWhenTableHasData() {
        when(representativeFoodRepository.count()).thenReturn(1L);

        assertThat(service.initializeRepresentativeFoodsIfEmpty()).isZero();

        verify(exApiClient, never()).getRepresentativeFoodServiceArea(any(Integer.class));
    }

    @Test
    @DisplayName("대표 음식 테이블이 비어 있으면 초기 동기화를 실행한다")
    void initialize_refreshesWhenTableIsEmpty() {
        when(representativeFoodRepository.count()).thenReturn(0L);
        when(exApiClient.getRepresentativeFoodServiceArea(1))
                .thenReturn(response(1, item("A00001", "휴게소", "메뉴", "가격")));

        assertThat(service.initializeRepresentativeFoodsIfEmpty()).isEqualTo(1);
    }

    @Test
    @DisplayName("페이지에 목록이 없거나 휴게소 코드가 비어 있으면 저장을 거부한다")
    void refresh_rejectsMissingServiceAreaCodeAndAllowsNullList() {
        when(exApiClient.getRepresentativeFoodServiceArea(1)).thenReturn(responseWithoutList(1));
        assertThat(service.refreshRepresentativeFoods()).isZero();

        RestStopEntity blankCode = org.mockito.Mockito.mock(RestStopEntity.class);
        when(blankCode.getServiceAreaCode()).thenReturn("   ");
        RestStopEntity nullCode = org.mockito.Mockito.mock(RestStopEntity.class);
        when(nullCode.getServiceAreaCode()).thenReturn(null);
        RestStopEntity duplicateCode = org.mockito.Mockito.mock(RestStopEntity.class);
        when(duplicateCode.getServiceAreaCode()).thenReturn("A00001");
        when(restStopRepository.findAll()).thenReturn(List.of(restStop, blankCode, nullCode, duplicateCode));

        when(exApiClient.getRepresentativeFoodServiceArea(1))
                .thenReturn(response(1, item("A00001", "휴게소", "메뉴", "가격")));
        assertThat(service.refreshRepresentativeFoods()).isEqualTo(1);

        when(exApiClient.getRepresentativeFoodServiceArea(1)).thenReturn(response(1, item("", "휴게소", "메뉴", "가격")));
        assertThatThrownBy(() -> service.refreshRepresentativeFoods())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("serviceAreaCode");

        when(exApiClient.getRepresentativeFoodServiceArea(1)).thenReturn(responseWithItems(1, itemWithNullCode()));
        assertThatThrownBy(() -> service.refreshRepresentativeFoods())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("serviceAreaCode");
    }

    @Test
    @DisplayName("같은 휴게소와 방향의 대표 음식이 중복되면 저장을 거부한다")
    void refresh_rejectsDuplicateNaturalKey() {
        RepresentativeFoodItem first = item("A00001", "휴게소", "메뉴1", "가격1");
        RepresentativeFoodItem second = item("A00001", "휴게소", "메뉴2", "가격2");
        when(exApiClient.getRepresentativeFoodServiceArea(1)).thenReturn(responseWithItems(1, first, second));

        assertThatThrownBy(() -> service.refreshRepresentativeFoods())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("중복");
    }

    private RepresentativeFoodResponse response(int pageSize, RepresentativeFoodItem item) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(
                            "{\"code\":\"SUCCESS\",\"pageSize\":" + pageSize + ",\"list\":[{"
                                    + "\"serviceAreaCode\":\"" + item.getServiceAreaCode() + "\","
                                    + "\"serviceAreaName\":\"" + item.getServiceAreaName() + "\","
                                    + "\"batchMenu\":\"" + item.getBatchMenu() + "\","
                                    + "\"salePrice\":\"" + item.getSalePrice() + "\"}]}",
                            RepresentativeFoodResponse.class);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private RepresentativeFoodResponse responseWithoutList(int pageSize) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(
                            "{\"code\":\"SUCCESS\",\"pageSize\":" + pageSize + "}", RepresentativeFoodResponse.class);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private RepresentativeFoodResponse responseWithItems(int pageSize, RepresentativeFoodItem... items) {
        try {
            String list = java.util.Arrays.stream(items)
                    .map(item -> "{\"serviceAreaCode\":"
                            + (item.getServiceAreaCode() == null ? "null" : "\"" + item.getServiceAreaCode() + "\"")
                            + ",\"batchMenu\":\"" + item.getBatchMenu() + "\",\"salePrice\":\""
                            + item.getSalePrice() + "\"}")
                    .collect(java.util.stream.Collectors.joining(","));
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(
                            "{\"code\":\"SUCCESS\",\"pageSize\":" + pageSize + ",\"list\":[" + list + "]}",
                            RepresentativeFoodResponse.class);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private RepresentativeFoodItem item(String code, String name, String menu, String price) {
        try {
            RepresentativeFoodItem item = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(
                            "{\"serviceAreaCode\":\"" + code + "\",\"serviceAreaName\":\"" + name
                                    + "\",\"batchMenu\":\"" + menu + "\",\"salePrice\":\"" + price + "\"}",
                            RepresentativeFoodItem.class);
            return item;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private RepresentativeFoodItem itemWithNullCode() {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue("{\"serviceAreaCode\":null,\"batchMenu\":\"메뉴\"}", RepresentativeFoodItem.class);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
