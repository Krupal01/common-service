package com.krunish.common.security.generic;

/**
 * Marker interface that GenericJwtFilter implements.
 *
 * Used as the @ConditionalOnMissingBean and injection type in
 * GenericCommonAutoConfiguration so that:
 *
 *   1. A service-provided custom filter (even if it doesn't extend GenericJwtFilter)
 *      suppresses the default GenericJwtFilter registration — as long as it
 *      implements AuthFilter.
 *
 *   2. SecurityFilterChain injects AuthFilter (not the concrete class), keeping
 *      the auto-config decoupled from the implementation.
 *
 * Services that need a custom filter:
 *
 *   @Component
 *   public class MyCustomFilter extends OncePerRequestFilter implements AuthFilter {
 *       // custom logic
 *   }
 *   // GenericJwtFilter will NOT be registered because AuthFilter bean already exists
 */
public interface AuthFilter extends jakarta.servlet.Filter {
    // Marker — no methods required.
    // Spring's OncePerRequestFilter already implements jakarta.servlet.Filter,
    // so AuthFilter just needs to be present on the class.
}