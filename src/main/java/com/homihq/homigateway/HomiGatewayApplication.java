package com.homihq.homigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class HomiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomiGatewayApplication.class, args);
    }

}
