package com.homihq.homigateway.route;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Objects;


@Component
@Slf4j
@RequiredArgsConstructor
public class DynamicRouteScheduling {

    private final WebClient webClient;

    private final DynamicRouteService dynamicRouteService;

    @Value("${homi.platform.url}")
    private String PLATFORM_URL;

    @Value("{homi.orgId}")
    private String ORG_ID;

    @Value("{homi.apiKeyId}")
    private String APIKEY_ID;


    private Long versionId = 0L;


    @Scheduled(cron = "@hourly")
    public void fetchRoutes() {

        log.info("Current Route Version : {}" , versionId);
        webClient.get()
                .uri(PLATFORM_URL)
                .header("X-API-KEY", APIKEY_ID)
                .header("X-ORG_ID", ORG_ID)
                .header("X-ROUTE-VERSION", versionId + "")
                .retrieve().bodyToMono(DynamicRouteDefinitions.class)
                .subscribe(
                        routeDefs -> {
                            this.versionId = routeDefs.getVersion();
                            if(!Objects.isNull(routeDefs.getRouteDefinitions()) &&
                                !routeDefs.getRouteDefinitions().isEmpty()) {
                                this.dynamicRouteService.addAll(routeDefs.getRouteDefinitions());
                            }
                        },
                        error -> {log.error("Error retrieving routes - {}", error);},
                        () -> {log.info("Completed fetching routes.");}
                );

    }



}
