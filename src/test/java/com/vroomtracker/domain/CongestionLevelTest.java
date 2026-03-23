package com.vroomtracker.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CongestionLevelTest {

    @Test
    @DisplayName("from_highThreshold이상이면HIGH반환")
    void from_returnsHighWhenAtOrAboveHighThreshold() {
        assertThat(CongestionLevel.from(5.0)).isEqualTo(CongestionLevel.HIGH);
        assertThat(CongestionLevel.from(10.0)).isEqualTo(CongestionLevel.HIGH);
    }

    @Test
    @DisplayName("from_mediumThreshold이상highThreshold미만이면MEDIUM반환")
    void from_returnsMediumWhenBetweenThresholds() {
        assertThat(CongestionLevel.from(2.0)).isEqualTo(CongestionLevel.MEDIUM);
        assertThat(CongestionLevel.from(4.9)).isEqualTo(CongestionLevel.MEDIUM);
    }

    @Test
    @DisplayName("from_mediumThreshold미만이면LOW반환")
    void from_returnsLowWhenBelowMediumThreshold() {
        assertThat(CongestionLevel.from(0.0)).isEqualTo(CongestionLevel.LOW);
        assertThat(CongestionLevel.from(1.9)).isEqualTo(CongestionLevel.LOW);
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({"HIGH, 많음", "MEDIUM, 보통", "LOW, 적음"})
    @DisplayName("label_각레벨에맞는한글라벨반환")
    void label_returnsKoreanLabelForEachLevel(CongestionLevel level, String expected) {
        assertThat(level.label()).isEqualTo(expected);
    }
}
