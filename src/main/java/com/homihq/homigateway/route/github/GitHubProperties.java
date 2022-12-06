package com.homihq.homigateway.route.github;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Deprecated
@Data
@ConfigurationProperties(GitHubProperties.PREFIX)
public class GitHubProperties {
    /**
     * Properties prefix.
     */
    public static final String PREFIX = "homi.config.route.github";

    private String type;
    private String token;
    private String repo;
    private String branch;
    private String file;

}
