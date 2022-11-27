package com.homihq.homigateway.route;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("homi.config")
public class RouteConfig {
    private String type;
    private String token;
    private String repo;
    private String branch;
    private String file;

}
