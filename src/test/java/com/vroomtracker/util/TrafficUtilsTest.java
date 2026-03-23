package com.vroomtracker.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrafficUtilsTest {

    @Test
    @DisplayName("parseAmount_숫자문자열을double로반환")
    void parseAmount_returnsDoubleValue() {
        assertThat(TrafficUtils.parseAmount("12.5")).isEqualTo(12.5);
        assertThat(TrafficUtils.parseAmount("  100  ")).isEqualTo(100.0);
    }

    @Test
    @DisplayName("parseAmount_숫자가아니면0반환")
    void parseAmount_returnsZeroForNonNumeric() {
        assertThat(TrafficUtils.parseAmount("abc")).isEqualTo(0.0);
        assertThat(TrafficUtils.parseAmount("")).isEqualTo(0.0);
    }

    @Test
    @DisplayName("formatSumTm_10자리yyyyMMddHH를yyyy-MM-dd-HH시로변환")
    void formatSumTm_formats10DigitString() {
        assertThat(TrafficUtils.formatSumTm("2026032314")).isEqualTo("2026-03-23 14시");
    }

    @Test
    @DisplayName("formatSumTm_12자리yyyyMMddHHmm를yyyy-MM-dd-HH:mm으로변환")
    void formatSumTm_formats12DigitString() {
        assertThat(TrafficUtils.formatSumTm("202603231430")).isEqualTo("2026-03-23 14:30");
    }

    @Test
    @DisplayName("formatSumTm_null이거나짧으면원본반환")
    void formatSumTm_returnsOriginalWhenNullOrShort() {
        assertThat(TrafficUtils.formatSumTm(null)).isNull();
        assertThat(TrafficUtils.formatSumTm("2026")).isEqualTo("2026");
    }
}
