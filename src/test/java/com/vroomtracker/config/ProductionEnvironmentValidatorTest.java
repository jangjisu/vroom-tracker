package com.vroomtracker.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductionEnvironmentValidatorTest {

    @Test
    @DisplayName("운영 키가 모두 실제 값이면 검증을 통과한다")
    void afterPropertiesSet_withRealValues_passes() {
        ProductionEnvironmentValidator validator =
                new ProductionEnvironmentValidator("real-ex-key", "real-kakao-key", "real-naver-key");

        assertThatCode(validator::afterPropertiesSet).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("운영 키가 비어 있으면 부팅을 실패시킨다")
    void afterPropertiesSet_withBlankValue_fails() {
        ProductionEnvironmentValidator validator =
                new ProductionEnvironmentValidator("real-ex-key", " ", "real-naver-key");

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
                new ProductionEnvironmentValidator("real-ex-key", "real-kakao-key", null);

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
                new ProductionEnvironmentValidator("your-api-key", "placeholder", "change-me");

        assertThatThrownBy(validator::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EX_API_KEY")
                .hasMessageContaining("KAKAO_REST_API_KEY")
                .hasMessageContaining("NAVER_MAPS_NCP_KEY_ID")
                .hasMessageNotContaining("your-api-key")
                .hasMessageNotContaining("placeholder")
                .hasMessageNotContaining("change-me");
    }

    @Test
    @DisplayName("운영 키가 기본 placeholder이면 부팅을 실패시킨다")
    void afterPropertiesSet_withDefaultPlaceholderValue_fails() {
        ProductionEnvironmentValidator validator =
                new ProductionEnvironmentValidator("YOUR_API_KEY_HERE", "real-kakao-key", "real-naver-key");

        assertThatThrownBy(validator::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EX_API_KEY")
                .hasMessageNotContaining("YOUR_API_KEY_HERE");
    }
}
