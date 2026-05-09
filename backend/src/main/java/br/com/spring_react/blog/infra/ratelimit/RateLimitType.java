package br.com.spring_react.blog.infra.ratelimit;

import lombok.Getter;

import java.time.Duration;

@Getter
public enum RateLimitType {
    // nome do limiter, qtd. de tentativas e tempo de bloqueio
    GLOBAL(100, Duration.ofMinutes(1)),
    LOGIN(5, Duration.ofMinutes(15)),
    SIGNUP(3, Duration.ofHours(1)),
    POST_CREATION(10, Duration.ofDays(1)),
    COMMENT_CREATION(5, Duration.ofMinutes(1));

    private final int capacity;
    private final Duration refillDuration;

    // constructor que instancia os limiters acima
    RateLimitType(int capacity, Duration refillDuration) {
        this.capacity = capacity;
        this.refillDuration = refillDuration;
    }
}
