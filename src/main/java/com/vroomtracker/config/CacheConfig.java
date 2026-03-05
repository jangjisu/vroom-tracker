package com.vroomtracker.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    /**
     * 캐시 전략:
     * - dashboard: 5분 TTL
     *   getDashboardData() 에만 적용. trafficIc API 15분 집계 기준.
     *   개별 fetch 메서드에 @Cacheable 없음 (자기 호출 시 AOP 우회 문제 방지).
     *
     * trafficFlowByTime 데이터는 DB에서 읽으므로 별도 캐시 불필요.
     */
    @Bean
    public CacheManager cacheManager() {
        var dashboard = new CaffeineCache("dashboard",
                Caffeine.newBuilder()
                        .maximumSize(10)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build());

        var manager = new SimpleCacheManager();
        manager.setCaches(List.of(dashboard));
        return manager;
    }
}
