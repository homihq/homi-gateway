package com.homihq.homigateway.route;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubRunner implements CommandLineRunner {
    private final RouteConfig routeConfig;
    //33edb09ea3509d0325e4371233ab464ed44f1ba7

    @Override
    public void run(String... args) throws Exception {
        log.info("Route config - {}", routeConfig);
        GitHub gitHub =
        GitHubBuilder.fromEnvironment()
                .withOAuthToken(this.routeConfig.getToken()).build();
        GHRepository ghRepository =
        gitHub.getRepository(routeConfig.getRepo());


        GHContent ghContent =
        ghRepository.getFileContent(routeConfig.getFile(),  routeConfig.getBranch());
        log.info("ghContent - {}", ghContent);
        log.info("Is file - {}", ghContent.isFile());
        log.info("sha - {}", ghContent.getSha());


        Yaml yaml = new Yaml();
        Map<String,List<RouteDefinition>> routes = yaml.load(ghContent.read());
        log.info("routes - {}", routes);
    }
}
