package com.example.apigatewayservice.filters;


import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;


import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Component
public class AuthFilter extends AbstractGatewayFilterFactory<Config> {


    @Autowired
    private Environment env;
    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(final Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            if (!request.getPath().equals("/auth/reissue")){
                return chain.filter(exchange);
            }

            // Request Header 에 token 이 존재하지 않을 때
            if(!request.getHeaders().containsKey("Authorization")){
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            // Request Header 에서 token 문자열 받아오기
            List<String> token = request.getHeaders().get("Authorization");
            String tokenString = Objects.requireNonNull(token).get(0);
            String jwt = resolveToken(tokenString);

            System.out.println(jwt);




            return chain.filter(exchange); // 토큰이 일치할 때

        };



    }


    private String resolveToken(String rawToken) {
        String bearerToken = rawToken;
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }




}
