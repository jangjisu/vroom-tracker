package com.restroute.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BrandingConfigurationTest {

    @Test
    @DisplayName("사용자 노출 UI와 프로젝트 메타데이터는 rest-route 명칭을 사용한다")
    void visibleUiAndProjectMetadata_useRestRouteBranding() throws Exception {
        String index = Files.readString(Path.of("src/main/resources/templates/index.html"));
        String buildGradle = Files.readString(Path.of("build.gradle"));
        String packageJson = Files.readString(Path.of("package.json"));
        String packageLock = Files.readString(Path.of("package-lock.json"));

        assertThat(index)
                .contains("<title>rest-route</title>")
                .contains("rest-route")
                .doesNotContain(">vroom-tracker<");
        assertThat(packageJson).contains("\"name\": \"rest-route\"");
        assertThat(packageLock).contains("\"name\": \"rest-route\"");
        assertThat(buildGradle)
                .contains("property 'sonar.projectKey', 'rest-route'")
                .contains("property 'sonar.projectName', 'rest-route'");
    }

    @Test
    @DisplayName("프로젝트 문서와 내부 패키지 메타데이터는 rest-route 명칭을 사용한다")
    void projectDocsAndInternalPackageMetadata_useRestRouteBranding() throws Exception {
        String product = Files.readString(Path.of("PRODUCT.md"));
        String userInsights = Files.readString(Path.of("USER_INSIGHTS.md"));
        String agents = Files.readString(Path.of("AGENTS.md"));
        String workflow = Files.readString(Path.of("harness/WORKFLOW.md"));
        String buildGradle = Files.readString(Path.of("build.gradle"));

        assertThat(product).contains("`rest-route`").doesNotContain("`vroom-tracker`");
        assertThat(userInsights).contains("`rest-route`").doesNotContain("`vroom-tracker`");
        assertThat(agents).contains("# rest-route 작업 규칙").doesNotContain("`vroom-tracker`");
        assertThat(workflow).contains("# rest-route 작업 오케스트레이션");
        assertThat(buildGradle).contains("group = 'com.restroute'");
    }
}
