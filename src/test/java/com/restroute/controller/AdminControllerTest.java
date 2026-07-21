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

class AdminControllerTest {

    @Test
    @DisplayName("GET /admin은 관리자 대시보드 템플릿을 반환한다")
    void admin_returnsAdminDashboardView() {
        AdminDashboardService dashboardService = mock(AdminDashboardService.class);
        when(dashboardService.getSummary()).thenReturn(new AdminDashboardSummary(203, "2026-06", "준비중"));

        assertThat(new AdminController(
                                mock(SalesRankingUploadService.class),
                                mock(RestStopServiceAreaCodeBackfillService.class),
                                dashboardService)
                        .admin())
                .isEqualTo("admin-dashboard");
    }

    @Test
    @DisplayName("GET /admin/rest-stops/images는 휴게소 이미지 관리 템플릿을 반환한다")
    void restStopImages_returnsAdminRestStopImagesView() {
        AdminController controller = new AdminController(
                mock(SalesRankingUploadService.class),
                mock(RestStopServiceAreaCodeBackfillService.class),
                mock(AdminDashboardService.class));

        assertThat(controller.restStopImages()).isEqualTo("admin-rest-stop-images");
    }

    @Test
    @DisplayName("GET /admin/rest-stops/edit는 휴게소 정보 관리 템플릿을 반환한다")
    void restStopEdit_returnsAdminRestStopEditView() {
        AdminController controller = new AdminController(
                mock(SalesRankingUploadService.class),
                mock(RestStopServiceAreaCodeBackfillService.class),
                mock(AdminDashboardService.class));

        assertThat(controller.restStopEdit()).isEqualTo("admin-rest-stop-edit");
    }

    @Test
    @DisplayName("관리자 대시보드 API는 조회 요약을 반환한다")
    void dashboard_returnsSummary() {
        AdminDashboardSummary summary = new AdminDashboardSummary(203, "2026-06", "준비중");
        AdminDashboardService dashboardService = mock(AdminDashboardService.class);
        when(dashboardService.getSummary()).thenReturn(summary);
        AdminController controller = new AdminController(
                mock(SalesRankingUploadService.class),
                mock(RestStopServiceAreaCodeBackfillService.class),
                dashboardService);

        assertThat(controller.dashboard().getData()).isEqualTo(summary);
    }

    @Test
    @DisplayName("판매순위 업로드 후 관리자 화면으로 돌아간다")
    void uploadProductSalesRankings_redirectsToAdmin() {
        SalesRankingUploadService service = mock(SalesRankingUploadService.class);
        AdminController controller = new AdminController(
                service, mock(RestStopServiceAreaCodeBackfillService.class), mock(AdminDashboardService.class));
        MockMultipartFile product = new MockMultipartFile("productFile", "product.csv", "text/csv", new byte[] {1});

        assertThat(controller.uploadProductSalesRankings(product))
                .isEqualTo("redirect:/admin?upload=success&type=product");
        verify(service).uploadProducts(product);
    }

    @Test
    @DisplayName("매장 판매순위 업로드 후 관리자 화면으로 돌아간다")
    void uploadStoreSalesRankings_redirectsToAdmin() {
        SalesRankingUploadService service = mock(SalesRankingUploadService.class);
        AdminController controller = new AdminController(
                service, mock(RestStopServiceAreaCodeBackfillService.class), mock(AdminDashboardService.class));
        MockMultipartFile store = new MockMultipartFile("storeFile", "store.csv", "text/csv", new byte[] {1});

        assertThat(controller.uploadStoreSalesRankings(store)).isEqualTo("redirect:/admin?upload=success&type=store");
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
