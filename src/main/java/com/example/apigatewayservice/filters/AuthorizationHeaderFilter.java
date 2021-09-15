package com.example.apigatewayservice.filters;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<Config> {
	Environment env;

	public AuthorizationHeaderFilter(Environment env){
		this.env = env;
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			System.out.println("시작");
			if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
				return onError(exchange,"no authorization Header", HttpStatus.UNAUTHORIZED);
			}

			String authorization = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
			String jwt = authorization.replace("Bearer","");
			System.out.println("중간");
			System.out.println(jwt);

			if(!isJwtValid(jwt)){
				return onError(exchange,"JWT Token is not valid", HttpStatus.UNAUTHORIZED);
			}
			System.out.println("끝");
			return chain.filter(exchange);
		};
	}


	private boolean isJwtValid(String jwt){
		boolean returnValue = true;

		String subject = null;

		byte[] keyBytes = Decoders.BASE64.decode(env.getProperty("token.secret"));
		Key key = Keys.hmacShaKeyFor(keyBytes);

		try{
			subject = Jwts.parser().setSigningKey(key)
				.parseClaimsJws(jwt).getBody()
				.getSubject();
		}catch (Exception ex){
			returnValue = false;
		}

		System.out.println(subject);

		if(subject == null || subject.isEmpty() ){
			returnValue = false;
		}

		return returnValue;
	}

	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);

		return response.setComplete();
	}

}
