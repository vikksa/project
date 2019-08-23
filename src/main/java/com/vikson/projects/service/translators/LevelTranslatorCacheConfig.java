package com.vikson.projects.service.translators;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LevelTranslatorCacheConfig {

    @Bean
    public CacheManager levelsCacheManager() {
        CaffeineCacheManager profileCacheManager = new CaffeineCacheManager();
        profileCacheManager.setCaffeineSpec(CaffeineSpec.parse(getCacheSpec("30m","1000")));
        profileCacheManager.setAllowNullValues(false);
        return profileCacheManager;
    }
    private String getCacheSpec(String expireTime, String maxSize) {
        return String.format("expireAfterWrite=%s,maximumSize=%s", expireTime, maxSize);
    }


}
