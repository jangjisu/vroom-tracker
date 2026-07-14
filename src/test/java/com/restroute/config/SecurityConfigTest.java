package com.restroute.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.restroute.domain.AdminRole;
import com.restroute.domain.AdminUserEntity;
import com.restroute.repository.AdminUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.h2.console.enabled=false")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        adminUserRepository.deleteAll();
        adminUserRepository.saveAndFlush(
                AdminUserEntity.of("admin", passwordEncoder.encode("password"), AdminRole.ADMIN));
    }

    @Test
    @DisplayName("비로그인 사용자는 관리자 경로에서 로그인 폼으로 이동한다")
    void anonymousAdminRequest_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("비로그인 사용자는 관리자 대시보드 API에서 로그인 폼으로 이동한다")
    void anonymousAdminDashboardApi_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN 사용자는 관리자 경로의 보안 필터를 통과한다")
    void adminRequest_passesAuthorization() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(status().isOk()).andExpect(view().name("admin"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN 사용자는 관리자 대시보드 API를 조회할 수 있다")
    void adminDashboardApi_passesAuthorization() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("restStopCount")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("lastSyncStatus")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("관리자 화면은 CSRF가 포함된 로그아웃 폼을 렌더링한다")
    void adminView_rendersLogoutForm() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/logout\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("ADMIN이 아닌 인증 사용자는 관리자 경로에서 403을 받는다")
    void nonAdminRequest_returnsForbidden() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("ADMIN이 아닌 인증 사용자는 관리자 대시보드 API에서 403을 받는다")
    void nonAdminDashboardApi_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("기본 로그인 폼은 올바른 관리자 계정으로 /admin에 이동시킨다")
    void loginSuccess_redirectsToAdmin() throws Exception {
        mockMvc.perform(formLogin().user("admin").password("password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    @DisplayName("기본 로그인 폼은 잘못된 비밀번호를 거부한다")
    void loginFailure_redirectsWithError() throws Exception {
        mockMvc.perform(formLogin().user("admin").password("wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    @DisplayName("기존 공개 API는 로그인 없이 접근할 수 있다")
    void publicApi_isAccessibleWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/map-config")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("CSRF 토큰이 없는 로그인 요청은 거부한다")
    void loginWithoutCsrf_returnsForbidden() throws Exception {
        mockMvc.perform(post("/login").param("username", "admin").param("password", "password"))
                .andExpect(status().isForbidden());
    }
}
