package com.krunish.common.config;

import com.krunish.common.exception.GlobalExceptionHandler;
import com.krunish.common.security.AuthProperties;
import com.krunish.common.security.HibernateFilterConfigurer;
import com.krunish.common.security.aop.PermissionAspect;
import com.krunish.common.security.aop.PermissionChecker;
import com.krunish.common.security.generic.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;

/**
 * Generic auto-configuration — active only when:
 *   krunish.auth.mode=generic
 *
 * application.yml in your service:
 *   krunish:
 *     auth:
 *       mode: generic
 *
 * This config and CommonAutoConfiguration are mutually exclusive —
 * exactly one will load per service. Never both.
 */
@AutoConfiguration
@EnableConfigurationProperties(AuthProperties.class)
@ConditionalOnProperty(
        name = "krunish.auth.mode",
        havingValue = "generic"
        // matchIfMissing = false (default) — won't load unless explicitly set
)
@ComponentScan(basePackages = {
        "com.krunish.common.security.aop"
})
public class GenericCommonAutoConfiguration {

    // ── ClaimsExtractor ───────────────────────────────────────────────────────
    //
    // Falls back to DefaultClaimsExtractor (Long userId + email) when the
    // service doesn't register a custom ClaimsExtractor bean.
    //
    // New services override with:
    //   @Bean ClaimsExtractor<CrmAuthClaims> claimsExtractor() { ... }

    @Bean
    @ConditionalOnMissingBean(ClaimsExtractor.class)   // ← checks the INTERFACE, not the impl
    public ClaimsExtractor<DefaultAuthClaims> defaultClaimsExtractor() {
        return new DefaultClaimsExtractor();
    }

    // ── TokenValidator ────────────────────────────────────────────────────────
    //
    // Default: GenericJwtTokenValidator backed by the ClaimsExtractor above.
    // Replace entirely for opaque tokens or a custom JWT library:
    //   @Bean TokenValidator<MyClaims> tokenValidator() { ... }

    @Bean
    @ConditionalOnMissingBean(TokenValidator.class)    // ← checks the INTERFACE
    public <C extends AuthClaims> TokenValidator<C> tokenValidator(
            AuthProperties properties,
            ClaimsExtractor<C> claimsExtractor) {
        return new GenericJwtTokenValidator<>(properties, claimsExtractor);
    }

    // ── GenericJwtFilter ──────────────────────────────────────────────────────
    //
    // FIX: was @ConditionalOnMissingBean(GenericJwtFilter.class) which checked
    // the concrete class. Changed to check AuthFilter — the marker interface —
    // so a service-provided custom filter (subclass or alternative impl)
    // also suppresses this default registration.

    @Bean
    @ConditionalOnMissingBean(AuthFilter.class)        // ← checks the MARKER INTERFACE
    public <C extends AuthClaims> GenericJwtFilter<C> jwtFilter(
            TokenValidator<C> tokenValidator,
            Optional<GenericOrgAccessValidator> orgAccessValidator,
            AuthProperties properties) {

        GenericJwtFilter<C> filter = new GenericJwtFilter<>(tokenValidator, properties);
        orgAccessValidator.ifPresent(filter::setOrgAccessValidator);
        return filter;
    }

    // ── SecurityFilterChain ───────────────────────────────────────────────────
    //
    // Uses AuthFilter (marker interface) so it accepts both GenericJwtFilter
    // and any service-provided custom filter.

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthFilter jwtFilter,              // ← inject via marker interface
            AuthProperties properties) throws Exception {

        System.out.println(">>> [GenericCommonAutoConfig] Building SecurityFilterChain...");
        System.out.println(">>> [GenericCommonAutoConfig] Public paths: " + properties.getPublicPaths());

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(properties.getPublicPaths().toArray(new String[0]))
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);

        System.out.println(">>> [GenericCommonAutoConfig] ✅ SecurityFilterChain built");
        return http.build();
    }

    // ── Optional extras ───────────────────────────────────────────────────────
    //
    // FIX: HibernateFilterConfigurer was conditional on GenericOrgAccessValidator.class
    // (the interface). That's correct — keep it. It means: only register the
    // Hibernate filter if SOME bean implementing GenericOrgAccessValidator exists.

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(GenericOrgAccessValidator.class) // ← correct: checks the interface
    public HibernateFilterConfigurer hibernateFilterConfigurer() {
        return new HibernateFilterConfigurer();
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