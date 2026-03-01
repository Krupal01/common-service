package com.krunish.common.config;

import com.krunish.common.exception.GlobalExceptionHandler;
import com.krunish.common.security.*;
import com.krunish.common.security.aop.PermissionAspect;
import com.krunish.common.security.aop.PermissionChecker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@EnableConfigurationProperties({AuthSecurityProperties.class, AuthProperties.class})
@ComponentScan(basePackages = {
        "com.krunish.common.security.aop"  // only scan AOP, not all of security
})
public class CommonAutoConfiguration {

    // ✅ Always register these
    @Bean
    public JwtValidator jwtValidator(AuthSecurityProperties properties) {
        return new JwtValidator(properties);
    }

    @Bean
    @ConditionalOnBean(OrgAccessValidator.class)
    public JwtFilter jwtFilter(JwtValidator jwtValidator,
                               OrgAccessValidator orgAccessValidator,
                               AuthProperties properties) {
        return new JwtFilter(jwtValidator, orgAccessValidator, properties);
    }

    @Bean
    public HibernateFilterConfigurer hibernateFilterConfigurer(
            jakarta.persistence.EntityManager entityManager) {
        return new HibernateFilterConfigurer(entityManager);
    }

    @Bean
    @ConditionalOnBean(OrgAccessValidator.class)
    public AuthWrapper authWrapper(JwtValidator jwtValidator,
                                   OrgAccessValidator orgAccessValidator,
                                   AuthProperties properties) {
        return new AuthWrapper(jwtValidator, orgAccessValidator, properties);
    }

    @Bean
    @ConditionalOnBean(PermissionChecker.class)
    public PermissionAspect permissionAspect(PermissionChecker permissionChecker) {
        return new PermissionAspect(permissionChecker);
    }

    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}