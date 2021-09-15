package com.example.apigatewayservice.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties.Authentication;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UserFilter extends AbstractGatewayFilterFactory<Config> {
    public UserFilter() {
        super(Config.class);
    }

    @Autowired
    private Environment env;

    @Override
    public GatewayFilter apply(final Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            System.out.println("UserFilter");

            System.out.println("ddd");

            // Request Header 에 token 이 존재하지 않을 때
            if(!request.getHeaders().containsKey("Authorization")){
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            System.out.println("ddd");

            // Request Header 에서 token 문자열 받아오기
            List<String> token = request.getHeaders().get("Authorization");
            String tokenString = Objects.requireNonNull(token).get(0);
            String jwt = resolveToken(tokenString);

            System.out.println("토큰 : " + jwt );
            // 토큰 검증

            System.out.println(jwt);

            if(!isJwtValid(jwt)){
                System.out.println("에럴ㄹㄹㄹㄹㄹㄹㄹㄹ");
                return onError(exchange,"JWT Token is not valid", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange); // 토큰이 일치할 때

        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        return response.setComplete();
    }

    private boolean isJwtValid(String jwt){
        System.out.println("여기~~~~~!1");
        boolean returnValue = true;
        String subject = null;

        System.out.println("여기~~~~~!2");
        System.out.println(env.getProperty("token.secret"));
        System.out.println("여기~~~~~!2");


        try{
            System.out.println("여기~~~~~!3");
            subject = Jwts.parser().setSigningKey(env.getProperty("token.secret"))
                .parseClaimsJws(jwt).getBody().getSubject();
            System.out.println(subject);
        }catch (Exception ex){
            System.out.println("여기~~~~~!4");
            returnValue = false;
        }

        System.out.println("여기~~~~~!5");
        System.out.println(subject);

        if(subject == null || subject.isEmpty() ){
            returnValue = false;
        }

        return returnValue;
    }



    private String resolveToken(String rawToken) {
        String bearerToken = rawToken;
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}
