package com.krunish.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
@RequiredArgsConstructor
public class AuthWrapper {

    private final JwtValidator jwtValidator;
    private final OrgAccessValidator orgAccessValidator;
    private final AuthProperties properties;

//    public AuthWrapper(
//            JwtValidator jwtValidator,
//            OrgAccessValidator orgAccessValidator,
//            AuthProperties properties
//    ) {
//        this.jwtValidator = jwtValidator;
//        this.orgAccessValidator = orgAccessValidator;
//        this.properties = properties;
//    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtValidator, orgAccessValidator, properties);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(properties.getPublicPaths().toArray(new String[0]))
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
