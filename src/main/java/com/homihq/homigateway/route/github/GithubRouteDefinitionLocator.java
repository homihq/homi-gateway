package com.homihq.homigateway.route.github;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import reactor.core.publisher.Flux;
import static java.util.Collections.synchronizedMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class GithubRouteDefinitionLocator implements RouteDefinitionLocator {

    private final Map<String, RouteDefinition> routes = synchronizedMap(new LinkedHashMap<String, RouteDefinition>());


    public GithubRouteDefinitionLocator(){
        log.info("### GithubRouteDefinitionLocator ###");
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        Map<String, RouteDefinition> routesSafeCopy = new LinkedHashMap<>(routes);
        return Flux.fromIterable(routesSafeCopy.values());
    }
}
