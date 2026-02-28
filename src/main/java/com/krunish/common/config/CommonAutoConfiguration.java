package com.krunish.common.config;

import com.krunish.common.exception.GlobalExceptionHandler;
import com.krunish.common.security.AuthSecurityProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@EnableConfigurationProperties(AuthSecurityProperties.class)  // ✅ This registers the bean
@ComponentScan(basePackages = {
        "com.krunish.common.security",
        "com.krunish.common.exception"
})
public class CommonAutoConfiguration {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}