package com.tic.app.gateway.config;

import org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> gameEngineRoute() {
        return GatewayRouterFunctions.route("game-engine-route")
                .route(RequestPredicates.path("/api/games/**"), HandlerFunctions.http())
                .filter(LoadBalancerFilterFunctions.lb("game-engine-service-app"))
                .filter(FilterFunctions.stripPrefix(1))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> gameSessionRoute() {
        return GatewayRouterFunctions.route("game-session-route")
                .route(RequestPredicates.path("/api/sessions/**"), HandlerFunctions.http())
                .filter(LoadBalancerFilterFunctions.lb("game-session-service-app"))
                .filter(FilterFunctions.stripPrefix(1))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> uiRoute() {
        return GatewayRouterFunctions.route("ui-route")
                .route(RequestPredicates.path("/**"), HandlerFunctions.http())
                .filter(LoadBalancerFilterFunctions.lb("ui-service-app"))
                .build();
    }
}
