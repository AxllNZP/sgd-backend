package com.mesapartes.sgd.security;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.*;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private final LoadingCache<String, Bucket> buckets;

    public RateLimitService() {
        this.buckets = Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build(this::createNewBucket);
    }

    private Bucket createNewBucket(String ip) {

        Bandwidth limit = Bandwidth.classic(
                5,
                Refill.intervally(5, Duration.ofMinutes(15))
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public boolean tryConsume(String ip) {
        Bucket bucket = buckets.get(ip);
        return bucket.tryConsume(1);
    }
}