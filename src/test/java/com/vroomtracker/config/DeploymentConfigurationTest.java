package com.vroomtracker.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeploymentConfigurationTest {

    @Test
    @DisplayName("prod 프로필은 MySQL datasource 환경변수를 사용한다")
    void prodProperties_useMysqlDatasourceEnvironmentVariables() throws Exception {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(Path.of("src/main/resources/application-prod.properties"))) {
            properties.load(reader);
        }

        assertThat(properties.getProperty("spring.datasource.driver-class-name"))
                .isEqualTo("com.mysql.cj.jdbc.Driver");
        assertThat(properties.getProperty("spring.datasource.url"))
                .isEqualTo(
                        "${SPRING_DATASOURCE_URL:jdbc:mysql://db:3306/vroom?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true}");
        assertThat(properties.getProperty("spring.datasource.username"))
                .isEqualTo("${SPRING_DATASOURCE_USERNAME:vroom}");
        assertThat(properties.getProperty("spring.datasource.password")).isEqualTo("${SPRING_DATASOURCE_PASSWORD:}");
    }

    @Test
    @DisplayName("docker compose는 앱과 MySQL DB 컨테이너를 함께 띄운다")
    void dockerCompose_definesMysqlServiceForApp() throws Exception {
        String compose = Files.readString(Path.of("docker-compose.yml"));

        assertThat(compose)
                .contains("app:")
                .contains("db:")
                .contains("image: mysql:8.4")
                .contains("MYSQL_DATABASE: ${MYSQL_DATABASE:-vroom}")
                .contains("mysql-data:/var/lib/mysql")
                .contains(
                        "SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/${MYSQL_DATABASE:-vroom}?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true")
                .contains("depends_on:")
                .contains("condition: service_healthy");
    }

    @Test
    @DisplayName("prod 배포는 앱 로그를 서버 logs 디렉토리에 날짜별 파일로 저장한다")
    void prodDeployment_writesDailyApplicationLogFiles() throws Exception {
        String compose = Files.readString(Path.of("docker-compose.yml"));
        String logback = Files.readString(Path.of("src/main/resources/logback-spring.xml"));
        String gitignore = Files.readString(Path.of(".gitignore"));

        assertThat(compose).contains("./logs:/app/logs");
        assertThat(logback)
                .contains("<springProfile name=\"prod\">")
                .contains("<appender-ref ref=\"CONSOLE\" />")
                .contains("<fileNamePattern>/app/logs/%d{yyyyMMdd}.log</fileNamePattern>");
        assertThat(gitignore).contains("logs/");
    }
}
