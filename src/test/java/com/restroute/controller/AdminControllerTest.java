package com.restroute.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restroute.service.RestStopServiceAreaCodeBackfillService;
import com.restroute.service.admindashboard.AdminDashboardService;
import com.restroute.service.admindashboard.AdminDashboardSummary;
import com.restroute.service.salesranking.SalesRankingUploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;

class AdminControllerTest {

    @Test
    @DisplayName("GET /admin은 관리자 템플릿을 반환한다")
    void admin_returnsAdminView() {
        AdminDashboardService dashboardService = mock(AdminDashboardService.class);
        when(dashboardService.getSummary()).thenReturn(new AdminDashboardSummary(203, "2026-06"));

        assertThat(new AdminController(
                                mock(SalesRankingUploadService.class),
                                mock(RestStopServiceAreaCodeBackfillService.class),
                                dashboardService)
                        .admin(mock(Model.class)))
                .isEqualTo("admin");
    }

    @Test
    @DisplayName("판매순위 업로드 후 관리자 화면으로 돌아간다")
    void uploadProductSalesRankings_redirectsToAdmin() {
        SalesRankingUploadService service = mock(SalesRankingUploadService.class);
        AdminController controller = new AdminController(
                service, mock(RestStopServiceAreaCodeBackfillService.class), mock(AdminDashboardService.class));
        MockMultipartFile product = new MockMultipartFile("productFile", "product.csv", "text/csv", new byte[] {1});

        assertThat(controller.uploadProductSalesRankings(product)).isEqualTo("redirect:/admin?upload=success");
        verify(service).uploadProducts(product);
    }

    @Test
    @DisplayName("매장 판매순위 업로드 후 관리자 화면으로 돌아간다")
    void uploadStoreSalesRankings_redirectsToAdmin() {
        SalesRankingUploadService service = mock(SalesRankingUploadService.class);
        AdminController controller = new AdminController(
                service, mock(RestStopServiceAreaCodeBackfillService.class), mock(AdminDashboardService.class));
        MockMultipartFile store = new MockMultipartFile("storeFile", "store.csv", "text/csv", new byte[] {1});

        assertThat(controller.uploadStoreSalesRankings(store)).isEqualTo("redirect:/admin?upload=success");
        verify(service).uploadStores(store);
    }

    @Test
    @DisplayName("판매순위 매핑 실행 후 관리자 화면으로 돌아간다")
    void backfillSalesRankings_redirectsToAdmin() {
        RestStopServiceAreaCodeBackfillService backfillService = mock(RestStopServiceAreaCodeBackfillService.class);
        AdminController controller = new AdminController(
                mock(SalesRankingUploadService.class), backfillService, mock(AdminDashboardService.class));

        assertThat(controller.backfillSalesRankings()).isEqualTo("redirect:/admin?backfill=success");
        verify(backfillService).backfill();
    }
}
