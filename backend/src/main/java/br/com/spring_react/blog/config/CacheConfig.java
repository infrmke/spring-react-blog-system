package br.com.spring_react.blog.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // cache para posts (listagem e slug)
        CaffeineCache postsCache = new CaffeineCache("posts",
                Caffeine.newBuilder()
                        .expireAfterWrite(10, TimeUnit.MINUTES) // TTL: 10 minutos
                        .maximumSize(500) // DESCREVER
                        .build());

        // cache para autores (listagem de posts por autor)
        CaffeineCache authorsCache = new CaffeineCache("authors",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES) // TTL: 5 minutos
                        .maximumSize(200)
                        .build());

        // cache para perfis
        CaffeineCache profilesCache = new CaffeineCache("profiles",
                Caffeine.newBuilder()
                        .expireAfterWrite(30, TimeUnit.MINUTES) // TTL: 30 minutos
                        .maximumSize(100)
                        .build());

        cacheManager.setCaches(Arrays.asList(postsCache, authorsCache, profilesCache));

        return cacheManager;
    }
}
