package com.homihq.homigateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayCommandLineRunner implements CommandLineRunner {


    @Override
    public void run(String... args) throws Exception {

        log.info(InetAddress.getLocalHost().getHostName());
        log.info(InetAddress.getLoopbackAddress().getHostAddress());
        log.info(InetAddress.getLoopbackAddress().getHostName());

    }
}
