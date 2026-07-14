package com.restroute.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.restroute.service.salesranking.SalesRankingUploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class AdminControllerTest {

    @Test
    @DisplayName("GET /admin은 관리자 템플릿을 반환한다")
    void admin_returnsAdminView() {
        assertThat(new AdminController(mock(SalesRankingUploadService.class)).admin())
                .isEqualTo("admin");
    }

    @Test
    @DisplayName("판매순위 업로드 후 관리자 화면으로 돌아간다")
    void uploadProductSalesRankings_redirectsToAdmin() {
        SalesRankingUploadService service = mock(SalesRankingUploadService.class);
        AdminController controller = new AdminController(service);
        MockMultipartFile product = new MockMultipartFile("productFile", "product.csv", "text/csv", new byte[] {1});

        assertThat(controller.uploadProductSalesRankings(product)).isEqualTo("redirect:/admin?upload=success");
        verify(service).uploadProducts(product);
    }

    @Test
    @DisplayName("매장 판매순위 업로드 후 관리자 화면으로 돌아간다")
    void uploadStoreSalesRankings_redirectsToAdmin() {
        SalesRankingUploadService service = mock(SalesRankingUploadService.class);
        AdminController controller = new AdminController(service);
        MockMultipartFile store = new MockMultipartFile("storeFile", "store.csv", "text/csv", new byte[] {1});

        assertThat(controller.uploadStoreSalesRankings(store)).isEqualTo("redirect:/admin?upload=success");
        verify(service).uploadStores(store);
    }
}
