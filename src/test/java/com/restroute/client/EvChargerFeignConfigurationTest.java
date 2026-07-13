package com.restroute.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureJsonTesters
class EvChargerFeignConfigurationTest {

    @Autowired
    private FeignClientProperties feignClientProperties;

    @Test
    @DisplayName("전기차 API Feign Client는 rest-route User-Agent와 JSON Accept를 사용한다")
    void evChargerApi_appliesDefaultHeaders() {
        FeignClientProperties.FeignClientConfiguration configuration =
                feignClientProperties.getConfig().get("ev-charger-api");
        Map<String, Collection<String>> headers = configuration.getDefaultRequestHeaders();

        assertThat(headers)
                .containsEntry("User-Agent", List.of("rest-route"))
                .containsEntry("Accept", List.of("application/json"));
    }
}
