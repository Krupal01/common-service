package com.krunish.common.security;

import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.AntPathMatcher;

import javax.crypto.SecretKey;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private List<String> publicPaths;
    private Security security = new Security();

    public boolean isPublic(String path) {
        AntPathMatcher matcher = new AntPathMatcher();
        return publicPaths != null &&
                publicPaths.stream().anyMatch(pattern -> matcher.match(pattern, path));
    }

    @Data
    public static class Security {
        private String secret;

        public SecretKey getSigningKey() {
            return Keys.hmacShaKeyFor(secret.getBytes());
        }
    }
}
