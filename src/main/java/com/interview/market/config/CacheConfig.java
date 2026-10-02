package com.interview.market.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * Caching for expensive-to-obtain results.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                new CaffeineCache("coefficients", coefficientsCache()),
                new CaffeineCache("sensitivity", sensitivityCache())));
        return manager;
    }

    private Cache<Object, Object> coefficientsCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(20))
                .maximumSize(10)
                .build();
    }

    private Cache<Object, Object> sensitivityCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(100)
                .build();
    }
}
