package com.homihq.homigateway.route.github;

import com.homihq.homigateway.route.github.hook.GitHubWebhookHandler;
import com.homihq.homigateway.route.github.hook.GitHubWebhookHandlerFilterFunction;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
@ConditionalOnProperty(
        value="homi.route.github.enabled",
        havingValue = "true")
public class GitHubRouteDefinitionConfiguration {

    @Bean
    public GithubRouteDefinitionLocator githubRouteDefinitionLocator() {
        return new GithubRouteDefinitionLocator();
    }

    @Bean
    public GitHubProperties gitHubProperties() {
        return new GitHubProperties();
    }

    @Bean
    public GitHubWebhookHandler gitHubWebhookHandler() {
        return new GitHubWebhookHandler();
    }
    @Bean
    public GitHubWebhookHandlerFilterFunction gitHubWebhookHandlerFilterFunction() {
        return new GitHubWebhookHandlerFilterFunction();
    }

    @Bean
    public RouterFunction<ServerResponse> route(GitHubWebhookHandler gitHubWebhookHandler) {
        return RouterFunctions
                .route(POST("/webhooks/{gwId}"), gitHubWebhookHandler::handle)
                .filter(gitHubWebhookHandlerFilterFunction());
    }
}
