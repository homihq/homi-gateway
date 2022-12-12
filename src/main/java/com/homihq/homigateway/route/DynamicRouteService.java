package com.homihq.homigateway.route;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
class DynamicRouteService {


    private final RouteDefinitionWriter routeDefinitionWriter;
    private final ApplicationEventPublisher publisher;

    public void addAll(List<RouteDefinition> routeDefinitions) {
        for(RouteDefinition routeDefinition : routeDefinitions) {
            add(routeDefinition);
        }
    }

    public void add(RouteDefinition definition) {
        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        this.publisher.publishEvent(new RefreshRoutesEvent(this));

    }

    public void update(RouteDefinition definition) {
        try {
            delete(definition.getId());
        } catch (Exception e) {
            log.info("Route update failed,could not find route  routeId: " + definition.getId());
        }

        add(definition);

    }

    public void delete(String id) {
        this.routeDefinitionWriter.delete(Mono.just(id)).subscribe();
    }
}
