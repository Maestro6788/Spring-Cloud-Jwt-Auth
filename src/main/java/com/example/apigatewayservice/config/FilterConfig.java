package com.example.apigatewayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 라우터 직접 설정
 * yml에서 구현!!
 */

//@Configuration
public class FilterConfig {

  //@Bean
  public RouteLocator gatewayRoutes(RouteLocatorBuilder builder){
    return builder.routes()
        .route(r -> r.path("/user/**")
                    .filters(f -> f.addRequestHeader("user","user-header")
                    .addResponseHeader("user","user-header"))
                    .uri("http://localhost:8081"))
        .route(r -> r.path("/auth/**")
            .filters(f -> f.addRequestHeader("auth","auth-header")
                .addResponseHeader("auth","auth-header"))
            .uri("http://localhost:8081"))
        .build();
  }
}
