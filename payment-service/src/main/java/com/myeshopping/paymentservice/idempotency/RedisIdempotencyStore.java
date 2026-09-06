package com.myeshopping.paymentservice.idempotency;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Profile("postgres")
public class RedisIdempotencyStore {
    private static final Duration TTL = Duration.ofHours(24);
    private final StringRedisTemplate redisTemplate;

    public RedisIdempotencyStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Long getPaymentId(String idempotencyKey) {
        String value = redisTemplate.opsForValue().get(redisKey(idempotencyKey));
        return value == null ? null : Long.valueOf(value);
    }

    public void remember(String idempotencyKey, Long paymentId) {
        redisTemplate.opsForValue().set(redisKey(idempotencyKey), String.valueOf(paymentId), TTL);
    }

    private String redisKey(String idempotencyKey) {
        return "payment:idempotency:" + idempotencyKey;
    }
}
