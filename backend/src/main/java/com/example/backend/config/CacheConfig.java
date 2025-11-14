package com.example.backend.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for caching to improve application performance.
 * Enables caching for process definitions, user groups, deployments, and other frequently accessed data.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache manager configuration using ConcurrentMapCacheManager for simplicity.
     * In production, consider using Redis or Hazelcast for distributed caching.
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "processDefinitions",
            "userGroups", 
            "deployments",
            "processInstances",
            "taskDefinitions",
            "modelInfo",
            "engineInfo",
            "offers",
            // Engine info controller caches
            "engineProperties",
            "tableCounts",
            "jobStatistics",
            // Model controller caches
            "models",
            "model",
            "modelStatistics"
        );
    }
}