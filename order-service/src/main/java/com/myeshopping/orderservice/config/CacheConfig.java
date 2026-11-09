package com.myeshopping.orderservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.myeshopping.orderservice.dto.CustomerOrderTotal;
import com.myeshopping.orderservice.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    public static final String ORDERS          = "orders";
    public static final String CUSTOMER_ORDERS = "customerOrders";
    public static final String CUSTOMER_TOTALS = "customerTotals";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        JavaType orderType       = mapper.constructType(Order.class);
        JavaType orderListType   = mapper.getTypeFactory().constructCollectionType(List.class, Order.class);
        JavaType totalsListType  = mapper.getTypeFactory().constructCollectionType(List.class, CustomerOrderTotal.class);

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith("order-service::")
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults.entryTtl(Duration.ofMinutes(10)))
                .withInitialCacheConfigurations(Map.of(
                        ORDERS,          typed(defaults, mapper, orderType).entryTtl(Duration.ofMinutes(10)),
                        CUSTOMER_ORDERS, typed(defaults, mapper, orderListType).entryTtl(Duration.ofMinutes(5)),
                        CUSTOMER_TOTALS, typed(defaults, mapper, totalsListType).entryTtl(Duration.ofMinutes(1))))
                // Puts and evictions run after the transaction commits, so a rolled-back change never reaches Redis.
                .transactionAware()
                .build();
    }

    /** Redis being down should slow requests down, not fail them - fall through to the database. */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException ex, Cache cache, Object key) {
                log.warn("Cache get failed for {}::{}: {}", cache.getName(), key, ex.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException ex, Cache cache, Object key, Object value) {
                log.warn("Cache put failed for {}::{}: {}", cache.getName(), key, ex.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException ex, Cache cache, Object key) {
                log.error("Cache evict failed for {}::{} - entry may be stale until its TTL expires: {}", cache.getName(), key, ex.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException ex, Cache cache) {
                log.error("Cache clear failed for {}: {}", cache.getName(), ex.getMessage());
            }
        };
    }

    private static RedisCacheConfiguration typed(RedisCacheConfiguration base, ObjectMapper mapper, JavaType type) {
        return base.serializeValuesWith(SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(mapper, type)));
    }
}
