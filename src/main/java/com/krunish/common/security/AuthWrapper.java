package com.krunish.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//@Configuration
//@RequiredArgsConstructor
//public class AuthWrapper {
//
//    private final JwtValidator jwtValidator;
//    private final OrgAccessValidator orgAccessValidator;
//    private final AuthProperties properties;
//
//
//    @Bean
//    public JwtFilter jwtFilter() {
//        System.out.println(">>> [AuthWrapper] Creating JwtFilter...");
//        System.out.println(">>> [AuthWrapper] OrgAccessValidator present: " + (orgAccessValidator != null));
//        JwtFilter filter = new JwtFilter(jwtValidator, properties);
//        if (orgAccessValidator != null) {
//            filter.setOrgAccessValidator(orgAccessValidator);
//        }
//        return filter;
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        System.out.println(">>> [AuthWrapper] Building SecurityFilterChain...");
//        System.out.println(">>> [AuthWrapper] Public paths being permitted: " + properties.getPublicPaths());
//
//        if (properties.getPublicPaths() == null || properties.getPublicPaths().isEmpty()) {
//            System.out.println(">>> [AuthWrapper] ❌ WARNING — publicPaths is null/empty! All requests will require auth.");
//        }
//        http
//                .csrf(csrf -> {
//                    csrf.disable();
//                    System.out.println(">>> [AuthWrapper] CSRF disabled");
//                })
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(properties.getPublicPaths().toArray(new String[0]))
//                        .permitAll()
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
//
//        System.out.println(">>> [AuthWrapper] ✅ SecurityFilterChain built successfully");
//        return http.build();
//    }
//}
