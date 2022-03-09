package com.example.userservice.config;

import com.netflix.servo.util.TimeLimiter;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4J의 Circuit Breaker Config를 수정
 */

@Configuration
public class Resilience4JConfig {

    // Customize하기 원하는 것을 <>에 넣어는다.
    // UserServiceImpl에서 CircuitBreakerFactory를 수정해서 사용하기 위해서 아래와 같이 수정
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {

        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(4) // CircuitBreaker를 열지 결정하는 Threshold. default 50
                .waitDurationInOpenState(Duration.ofMillis(1000)) // CircuitBreaker를 open한 상태로 유지하는 지속 시간. default 60s
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) // CircuitBreaker가 닫힐 때 통화 결과를 기록하는데 사용되는 슬라이딩 창의 유형. 카운트 기반 또는 시간 기반
                .slidingWindowSize(2) // CircuitBreaker가 닫힐 때 호출 결과를 기록하는데 사용되는 슬라이딩 창의 크기. default 100
                .build();

        // TimeLimiter는 future supplier의 time limit를 정하는 API
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(4)) // default 1s
                .build();


        // factory를 반환할 것인데 -> 그 factory의 설정은 이렇다.
        return factory -> factory.configureDefault(id ->
                new Resilience4JConfigBuilder(id)
                        .timeLimiterConfig(timeLimiterConfig)
                        .circuitBreakerConfig(circuitBreakerConfig)
                        .build());
    }
}
