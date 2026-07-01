package com.restroute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 애플리케이션 컨텍스트 로드 테스트.
 * 테스트 설정은 src/test/resources/application-test.properties 참고.
 */
@SpringBootTest
@ActiveProfiles("test")
class RestRouteApplicationTests {

    @Test
    @DisplayName("Spring Application Context 가 정상적으로 로드된다")
    void contextLoads() {}

    @Test
    @DisplayName("main 메서드는 RestRouteApplication 을 실행한다")
    void main_runsRestRouteApplication() {
        String[] args = {"--spring.profiles.active=test"};

        try (MockedStatic<SpringApplication> springApplication = Mockito.mockStatic(SpringApplication.class)) {
            RestRouteApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(RestRouteApplication.class, args));
        }
    }
}
