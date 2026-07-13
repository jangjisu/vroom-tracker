package com.restroute.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        mockMvc.perform(get("/admin/anything"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN 사용자는 관리자 경로의 보안 필터를 통과한다")
    void adminRequest_passesAuthorization() throws Exception {
        mockMvc.perform(get("/admin/anything"))
                .andExpect(
                        result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(403));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("ADMIN이 아닌 인증 사용자는 관리자 경로에서 403을 받는다")
    void nonAdminRequest_returnsForbidden() throws Exception {
        mockMvc.perform(get("/admin/anything")).andExpect(status().isForbidden());
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
    @DisplayName("H2 Console이 비활성화된 프로필에서는 접근할 수 없다")
    void h2Console_isDeniedWhenDisabled() throws Exception {
        mockMvc.perform(get("/h2-console/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("CSRF 토큰이 없는 로그인 요청은 거부한다")
    void loginWithoutCsrf_returnsForbidden() throws Exception {
        mockMvc.perform(post("/login").param("username", "admin").param("password", "password"))
                .andExpect(status().isForbidden());
    }
}
