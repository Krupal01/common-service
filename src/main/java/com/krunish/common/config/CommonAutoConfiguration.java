package com.krunish.common.config;

import com.krunish.common.exception.GlobalExceptionHandler;
import com.krunish.common.security.*;
import com.krunish.common.security.aop.PermissionAspect;
import com.krunish.common.security.aop.PermissionChecker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;

@AutoConfiguration
@EnableConfigurationProperties(AuthProperties.class)
@ComponentScan(basePackages = {
        "com.krunish.common.security.aop"  // only scan AOP, not all of security
})
public class CommonAutoConfiguration {

    // ✅ Always register these
    @Bean
    @ConditionalOnMissingBean
    public JwtValidator jwtValidator(AuthProperties properties) {
        return new JwtValidator(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtFilter jwtFilter(JwtValidator jwtValidator,
                               Optional<OrgAccessValidator> orgAccessValidator,
                               AuthProperties properties) {
        JwtFilter filter = new JwtFilter(jwtValidator, properties);
        orgAccessValidator.ifPresent(filter::setOrgAccessValidator);
        return filter;
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtFilter jwtFilter,
            AuthProperties properties
            ) throws Exception {
        System.out.println(">>> [AuthWrapper] Building SecurityFilterChain...");
        System.out.println(">>> [AuthWrapper] Public paths being permitted: " + properties.getPublicPaths());

        if (properties.getPublicPaths() == null || properties.getPublicPaths().isEmpty()) {
            System.out.println(">>> [AuthWrapper] ❌ WARNING — publicPaths is null/empty! All requests will require auth.");
        }
        http
                .csrf(csrf -> {
                    csrf.disable();
                    System.out.println(">>> [AuthWrapper] CSRF disabled");
                })
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(properties.getPublicPaths().toArray(new String[0]))
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        System.out.println(">>> [AuthWrapper] ✅ SecurityFilterChain built successfully");
        return http.build();
    }

//    @Bean
//    public AuthWrapper authWrapper(JwtValidator jwtValidator,
//                                   AuthProperties properties,
//                                   Optional<OrgAccessValidator> orgAccessValidator) {
//        System.out.println(">>> [AutoConfig] OrgAccessValidator present: " + orgAccessValidator.isPresent());
//        return new AuthWrapper(jwtValidator, orgAccessValidator.orElse(null), properties);
//    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OrgAccessValidator.class) // ✅ Only if service uses org filtering
    public HibernateFilterConfigurer hibernateFilterConfigurer() {
        return new HibernateFilterConfigurer(); // No constructor args — uses @PersistenceContext
    }

    @Bean
    @ConditionalOnMissingBean
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