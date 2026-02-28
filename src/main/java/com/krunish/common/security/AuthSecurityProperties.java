package com.krunish.common.security;

import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;

@Data
@ConfigurationProperties(prefix = "auth.security")
public class AuthSecurityProperties {

    private String secret;

    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
