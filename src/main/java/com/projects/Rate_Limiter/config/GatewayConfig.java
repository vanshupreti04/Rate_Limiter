package com.projects.Rate_Limiter.config;

import com.projects.Rate_Limiter.filter.TokenBucketRateLimiterFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final TokenBucketRateLimiterFilter tokenBucketRateLimiterFilter;

    public GatewayConfig(TokenBucketRateLimiterFilter tokenBucketRateLimiterFilter) {
        this.tokenBucketRateLimiterFilter = tokenBucketRateLimiterFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Test backend route
                .route("test-backend-route", r -> r
                        .path("/api/test/**")
                        .filters(f -> f
                                .rewritePath("/api/test/(?<segment>.*)", "/real-backend/${segment}")
                                .filter(tokenBucketRateLimiterFilter.apply(new TokenBucketRateLimiterFilter.Config())))
                        .uri("http://localhost:8080"))

                // Public endpoints (no rate limiting)
                .route("public-endpoints", r -> r
                        .path(
                                "/gatway/health",
                                "/api/v1/health",
                                "/api/v1/register/**",
                                "/api/v1/reset-tokens"
                        )
                        .uri("http://localhost:8080"))

                // All other API endpoints (with rate limiting)
                .route("api-routes", r -> r
                        .path("/api/**")
                        .filters(f -> f
                                .filter(tokenBucketRateLimiterFilter.apply(new TokenBucketRateLimiterFilter.Config())))
                        .uri("http://localhost:8080"))
                .build();
    }
}