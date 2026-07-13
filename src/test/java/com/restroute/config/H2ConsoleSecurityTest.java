package com.restroute.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.h2.console.enabled=true", "spring.h2.console.path=/h2-console"})
class H2ConsoleSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("H2 Console이 활성화되면 콘솔 화면에 접근할 수 있다")
    void h2Console_isAccessibleWhenEnabled() throws Exception {
        mockMvc.perform(get("/h2-console/"))
                .andExpect(
                        result -> assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
    }

    @Test
    @DisplayName("H2 Console은 same-origin iframe 응답 헤더를 사용한다")
    void h2Console_allowsSameOriginFrames() throws Exception {
        mockMvc.perform(get("/h2-console/"))
                .andExpect(result -> assertThat(result.getResponse().getHeader("X-Frame-Options"))
                        .isEqualTo("SAMEORIGIN"));
    }

    @Test
    @DisplayName("H2 Console 경로는 local에서 CSRF 토큰 없이도 보안 필터를 통과한다")
    void h2Console_ignoresCsrfWhenEnabled() throws Exception {
        mockMvc.perform(post("/h2-console/")
                        .param("jdbcUrl", "jdbc:h2:mem:testdb")
                        .param("userName", "sa")
                        .param("password", ""))
                .andExpect(
                        result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(403));
    }
}
