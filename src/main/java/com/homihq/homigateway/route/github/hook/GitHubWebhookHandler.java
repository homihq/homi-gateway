package com.homihq.homigateway.route.github.hook;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Deprecated
public class GitHubWebhookHandler {

    public Mono<ServerResponse> handle(ServerRequest request) {
        Mono<String> gwId = Mono.just(request.pathVariable("gwId"));
        return ok().body(gwId, String.class);
    }
}


