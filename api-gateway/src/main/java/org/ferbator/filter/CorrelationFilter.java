package com.meeweel.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationFilter implements GlobalFilter, Ordered {
    public static final String CORRELATION_ID = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        var headers = exchange.getRequest().getHeaders();
        if (!headers.containsKey(CORRELATION_ID)) {
            exchange = exchange.mutate().request(
                    builder -> builder.header(CORRELATION_ID, UUID.randomUUID().toString())
            ).build();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
