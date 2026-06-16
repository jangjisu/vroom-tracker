package com.vroomtracker.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeConfigurationTest {

    @Test
    @DisplayName("시스템 기본 Clock bean을 생성한다")
    void clock_returnsSystemClock() {
        Clock clock = new TimeConfiguration().clock();

        assertThat(clock).isNotNull();
    }
}
