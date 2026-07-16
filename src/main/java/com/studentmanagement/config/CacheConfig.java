package com.studentmanagement.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching // Enables Spring Caching services
@Slf4j
public class CacheConfig {
    
    public CacheConfig() {
        log.info("Initialized Cache Manager: in-memory caching activated.");
    }
}
