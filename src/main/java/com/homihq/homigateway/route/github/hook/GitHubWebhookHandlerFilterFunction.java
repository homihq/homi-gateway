package com.homihq.homigateway.route.github.hook;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.Assert.*;

@Slf4j
@Deprecated
public class GitHubWebhookHandlerFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private static final int SIGNATURE_LENGTH = 64;

    @Value("${homi.gatewayId}")
    private String gatewayId;

    @Value("${GITHUB_WEBHOOK_SECRET:'1234'}")
    private String webHookSecret;

    @Override
    public Mono<ServerResponse> filter(ServerRequest serverRequest, HandlerFunction<ServerResponse> handlerFunction) {
        log.info("Inside filter handling webook");

        log.info("gatewayId = {}", gatewayId);
        log.info("gwId = {}", serverRequest.pathVariable("gwId"));

        if (!serverRequest.pathVariable("gwId").equalsIgnoreCase(gatewayId)) {
            return ServerResponse
                    .status(FORBIDDEN).build();
        }

        String signature = serverRequest.headers().firstHeader("x-hub-signature-256");

        log.info("signature = {}", signature);

        if(signature == null) {
            return ServerResponse
                    .status(FORBIDDEN).build();
        }

        serverRequest.bodyToMono(String.class)
                        .map(body -> {
                            log.info("body = {}", body);
                            if(isValidPayload(signature, body)) {
                                return true;
                            }
                            else {
                                throw new ResponseStatusException(FORBIDDEN);
                            }

                        }).subscribe();


        return handlerFunction.handle(serverRequest);
    }

    private boolean isValidPayload(String signature, String payload) {
        log.info("payload - {}", payload);
        String hmacHex = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, webHookSecret).hmacHex(payload);

        String computed = String.format("sha256=%s", hmacHex);
        boolean invalidLength = signature.length() != SIGNATURE_LENGTH;

        if (invalidLength || !constantTimeCompare(signature, computed)) {
            return false;
        }

        return true;
    }

    public static boolean constantTimeCompare(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(), b.getBytes());
    }

}
