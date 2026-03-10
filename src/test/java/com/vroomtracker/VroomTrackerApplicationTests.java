package com.vroomtracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * 애플리케이션 컨텍스트 로드 테스트.
 *
 * Feign 실제 호출을 막기 위해 ex.api.url 을 localhost 로 덮어씁니다.
 * (실제 API 연결 없이 Bean 구성만 검증)
 */
@SpringBootTest
@TestPropertySource(properties = {
        "ex.api.url=http://localhost",
        "ex.api.key=test-key"
})
class VroomTrackerApplicationTests {

    @Test
    @DisplayName("Spring Application Context 가 정상적으로 로드된다")
    void contextLoads() {
    }
}
