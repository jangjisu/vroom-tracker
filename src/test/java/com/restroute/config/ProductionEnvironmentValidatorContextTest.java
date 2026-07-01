package com.restroute.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ProductionEnvironmentValidatorContextTest {

    @Test
    @DisplayName("prod 프로필에서 placeholder 운영 키가 있으면 컨텍스트 시작을 실패시킨다")
    void context_withProdProfileAndPlaceholderValue_fails() {
        new ApplicationContextRunner()
                .withUserConfiguration(ProductionEnvironmentValidator.class)
                .withPropertyValues(
                        "spring.profiles.active=prod",
                        "ex.api.key=YOUR_API_KEY_HERE",
                        "kakao.rest-api-key=real-kakao-key",
                        "naver.maps.ncp-key-id=real-naver-key")
                .run(context -> assertThat(context.getStartupFailure())
                        .hasRootCauseInstanceOf(IllegalStateException.class)
                        .rootCause()
                        .hasMessageContaining("EX_API_KEY")
                        .hasMessageNotContaining("YOUR_API_KEY_HERE"));
    }

    @Test
    @DisplayName("prod 프로필에서 실제 운영 키가 모두 있으면 컨텍스트 시작을 통과한다")
    void context_withProdProfileAndRealValues_passes() {
        new ApplicationContextRunner()
                .withUserConfiguration(ProductionEnvironmentValidator.class)
                .withPropertyValues(
                        "spring.profiles.active=prod",
                        "ex.api.key=real-ex-key",
                        "kakao.rest-api-key=real-kakao-key",
                        "naver.maps.ncp-key-id=real-naver-key")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    @DisplayName("prod 프로필이 아니면 로컬 placeholder 기본값을 허용한다")
    void context_withoutProdProfile_allowsLocalPlaceholders() {
        new ApplicationContextRunner()
                .withUserConfiguration(ProductionEnvironmentValidator.class)
                .withPropertyValues("ex.api.key=YOUR_API_KEY_HERE", "kakao.rest-api-key=", "naver.maps.ncp-key-id=")
                .run(context -> assertThat(context).hasNotFailed());
    }
}
