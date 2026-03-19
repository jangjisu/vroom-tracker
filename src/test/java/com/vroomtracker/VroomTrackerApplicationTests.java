package com.vroomtracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 애플리케이션 컨텍스트 로드 테스트.
 * 테스트 설정은 src/test/resources/application-test.properties 참고.
 */
@SpringBootTest
@ActiveProfiles("test")
class VroomTrackerApplicationTests {

    @Test
    @DisplayName("Spring Application Context 가 정상적으로 로드된다")
    void contextLoads() {
    }
}
