package com.restroute.service.admindashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.restroute.domain.RestStopProductSalesRankEntity;
import com.restroute.domain.RestStopStoreSalesRankEntity;
import com.restroute.repository.RestStopProductSalesRankRepository;
import com.restroute.repository.RestStopRepository;
import com.restroute.repository.RestStopStoreSalesRankRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private RestStopRepository restStopRepository;

    @Mock
    private RestStopProductSalesRankRepository productRepository;

    @Mock
    private RestStopStoreSalesRankRepository storeRepository;

    private AdminDashboardService service;

    @BeforeEach
    void setUp() {
        service = new AdminDashboardService(restStopRepository, productRepository, storeRepository);
    }

    @Test
    void returnsRestStopCountAndLatestMonthAcrossBothRankingTables() {
        when(restStopRepository.count()).thenReturn(203L);
        when(productRepository.findTopByOrderByBaseYearMonthDesc()).thenReturn(Optional.of(product("2026-05")));
        when(storeRepository.findTopByOrderByBaseYearMonthDesc()).thenReturn(Optional.of(store("2026-06")));

        assertThat(service.getSummary()).isEqualTo(new AdminDashboardSummary(203L, "2026-06"));
    }

    @Test
    void returnsNullLatestMonthWhenRankingTablesAreEmpty() {
        when(restStopRepository.count()).thenReturn(0L);
        when(productRepository.findTopByOrderByBaseYearMonthDesc()).thenReturn(Optional.empty());
        when(storeRepository.findTopByOrderByBaseYearMonthDesc()).thenReturn(Optional.empty());

        assertThat(service.getSummary()).isEqualTo(new AdminDashboardSummary(0L, null));
    }

    private RestStopProductSalesRankEntity product(String month) {
        return RestStopProductSalesRankEntity.from(new com.restroute.service.salesranking.SalesRankingProductRow(
                month, "1", "S1", "휴게소", "M1", "매장", "P1", "상품"));
    }

    private RestStopStoreSalesRankEntity store(String month) {
        return RestStopStoreSalesRankEntity.from(
                new com.restroute.service.salesranking.SalesRankingStoreRow(month, "1", "1", "S1", "휴게소", "M1", "매장"));
    }
}
