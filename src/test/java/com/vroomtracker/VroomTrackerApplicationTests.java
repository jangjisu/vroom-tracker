package com.vroomtracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * 애플리케이션 컨텍스트 로드 테스트.
 *
 * - ex.api.url: Feign 실제 호출 방지
 * - datasource.url: 파일 기반 H2 대신 인메모리 H2 사용 (Hibernate Dialect 결정 가능)
 */
@SpringBootTest
@TestPropertySource(properties = {
        "ex.api.url=http://localhost",
        "ex.api.key=test-key",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class VroomTrackerApplicationTests {

    @Test
    @DisplayName("Spring Application Context 가 정상적으로 로드된다")
    void contextLoads() {
    }
}
