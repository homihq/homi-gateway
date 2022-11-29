package com.homihq.homigateway.redis;

import lombok.extern.slf4j.Slf4j;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
public class EmbededRedisServer {

    private RedisServer redisServer;



    @PostConstruct
    public void start() {
        log.info("About to start redis server.");
        if(redisServer == null) {
            redisServer = new RedisServer();
            redisServer.start();
            log.info("Redis server started.");
        }
    }

    @PreDestroy
    public void stop() {
        log.info("About to stop redis server.");
        if(redisServer == null) {
            redisServer.stop();
            log.info("Redis server stopped.");
        }
    }

}
