package com.homihq.homigateway.route;

import lombok.Data;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.util.List;

@Data
public class DynamicRouteDefinitions {

    private Long version;
    private List<RouteDefinition> routeDefinitions;
}
