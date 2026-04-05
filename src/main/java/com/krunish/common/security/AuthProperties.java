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
    private List<String> privatePaths;
    private Security security = new Security();

    public boolean isPublic(String path) {
        AntPathMatcher matcher = new AntPathMatcher();

        if (isPrivate(path)) {
            return false;
        }

        boolean result = publicPaths != null &&
                publicPaths.stream().anyMatch(pattern -> matcher.match(pattern, path));
        System.out.println(">>> [AuthProperties] isPublic('" + path + "') = " + result);
        System.out.println(">>> [AuthProperties] Configured publicPaths = " + publicPaths);
        return result;
    }

    public boolean isPrivate(String path) { // 👈 Added
        AntPathMatcher matcher = new AntPathMatcher();
        boolean result = privatePaths != null &&
                privatePaths.stream().anyMatch(pattern -> matcher.match(pattern, path));

        System.out.println(">>> [AuthProperties] isPrivate('" + path + "') = " + result);
        System.out.println(">>> [AuthProperties] Configured privatePaths = " + privatePaths);

        return result;
    }

    @Data
    public static class Security {
        private String secret;

        public SecretKey getSigningKey() {
            if (secret == null) {
                System.out.println(">>> [AuthProperties.Security] ❌ secret is NULL — check application.yml auth.security.secret");
            } else {
                System.out.println(">>> [AuthProperties.Security] ✅ secret loaded, length = " + secret.length());
            }
            return Keys.hmacShaKeyFor(secret.getBytes());
        }
    }
}
