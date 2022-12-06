package com.homihq.homigateway.route.github;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Deprecated
public class GithubService {
    private final GitHubProperties gitHubProperties;

    public void load() throws Exception {
        log.info("Route config - {}", gitHubProperties);
        GitHub gitHub =
        GitHubBuilder.fromEnvironment()
                .withOAuthToken(this.gitHubProperties.getToken()).build();
        GHRepository ghRepository =
        gitHub.getRepository(gitHubProperties.getRepo());


        GHContent ghContent =
        ghRepository.getFileContent(gitHubProperties.getFile(),  gitHubProperties.getBranch());
        log.info("ghContent - {}", ghContent);
        log.info("Is file - {}", ghContent.isFile());
        log.info("sha - {}", ghContent.getSha());


        Yaml yaml = new Yaml();
        Map<String,List<RouteDefinition>> routes = yaml.load(ghContent.read());
        log.info("routes - {}", routes);
    }
}
