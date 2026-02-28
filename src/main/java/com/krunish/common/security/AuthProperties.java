package com.krunish.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private List<String> publicPaths;

    public boolean isPublic(String path) {
        return publicPaths != null &&
                publicPaths.stream().anyMatch(path::startsWith);
    }
}
