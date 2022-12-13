package com.homihq.homigateway.route;

import lombok.Data;
import org.springframework.cloud.gateway.route.RouteDefinition;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class ApiDefinition {
    private String name;
    private String id;
    private String version;

    private List<RouteDefinition> routes;

    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;



}
