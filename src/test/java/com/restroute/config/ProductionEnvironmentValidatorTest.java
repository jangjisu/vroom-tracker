package com.restroute.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductionEnvironmentValidatorTest {

    @Test
    @DisplayName("운영 키가 모두 실제 값이면 검증을 통과한다")
    void afterPropertiesSet_withRealValues_passes() {
        ProductionEnvironmentValidator validator = new ProductionEnvironmentValidator(
                "real-ex-key", "real-kakao-key", "real-naver-key", "real-opinet-key");

        assertThatCode(validator::afterPropertiesSet).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("운영 키가 비어 있으면 부팅을 실패시킨다")
    void afterPropertiesSet_withBlankValue_fails() {
        ProductionEnvironmentValidator validator =
                new ProductionEnvironmentValidator("real-ex-key", " ", "real-naver-key", "real-opinet-key");

        assertThatThrownBy(validator::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("KAKAO_REST_API_KEY")
                .hasMessageNotContaining("real-ex-key")
                .hasMessageNotContaining("real-naver-key");
    }

    @Test
    @DisplayName("운영 키가 null이면 부팅을 실패시킨다")
    void afterPropertiesSet_withNullValue_fails() {
        ProductionEnvironmentValidator validator =
                new ProductionEnvironmentValidator("real-ex-key", "real-kakao-key", null, "real-opinet-key");

        assertThatThrownBy(validator::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("NAVER_MAPS_NCP_KEY_ID")
                .hasMessageNotContaining("real-ex-key")
                .hasMessageNotContaining("real-kakao-key");
    }

    @Test
    @DisplayName("운영 키가 placeholder이면 부팅을 실패시킨다")
    void afterPropertiesSet_withPlaceholderValue_fails() {
        ProductionEnvironmentValidator validator =
                new ProductionEnvironmentValidator("your-api-key", "placeholder", "change-me", "your-opinet-api-key");

        assertThatThrownBy(validator::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EX_API_KEY")
                .hasMessageContaining("KAKAO_REST_API_KEY")
                .hasMessageContaining("NAVER_MAPS_NCP_KEY_ID")
                .hasMessageContaining("OPINET_API_KEY")
                .hasMessageNotContaining("your-api-key")
                .hasMessageNotContaining("placeholder")
                .hasMessageNotContaining("change-me");
    }

    @Test
    @DisplayName("운영 키가 기본 placeholder이면 부팅을 실패시킨다")
    void afterPropertiesSet_withDefaultPlaceholderValue_fails() {
        ProductionEnvironmentValidator validator = new ProductionEnvironmentValidator(
                "YOUR_API_KEY_HERE", "real-kakao-key", "real-naver-key", "real-opinet-key");

        assertThatThrownBy(validator::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EX_API_KEY")
                .hasMessageNotContaining("YOUR_API_KEY_HERE");
    }

    @Test
    @DisplayName("운영 키가 replace-with-real 형태의 placeholder이면 부팅을 실패시킨다")
    void afterPropertiesSet_withReplaceWithRealPlaceholderValue_fails() {
        ProductionEnvironmentValidator validator = new ProductionEnvironmentValidator(
                "real-ex-key", "real-kakao-key", "real-naver-key", "replace-with-real-opinet-api-key");

        assertThatThrownBy(validator::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OPINET_API_KEY")
                .hasMessageNotContaining("replace-with-real-opinet-api-key");
    }

    @Test
    @DisplayName("your로 시작하지만 api key placeholder가 아니면 검증을 통과한다")
    void afterPropertiesSet_withYourPrefixButNotApiKeyPlaceholder_passes() {
        ProductionEnvironmentValidator validator = new ProductionEnvironmentValidator(
                "real-ex-key", "real-kakao-key", "real-naver-key", "your-real-opinet-token");

        assertThatCode(validator::afterPropertiesSet).doesNotThrowAnyException();
    }
}
