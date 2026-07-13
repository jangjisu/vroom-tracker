package com.restroute.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdminControllerTest {

    @Test
    @DisplayName("GET /admin은 관리자 템플릿을 반환한다")
    void admin_returnsAdminView() {
        assertThat(new AdminController().admin()).isEqualTo("admin");
    }
}
