package com.krunish.common.security.generic;

/**
 * Optional strategy for validating that the authenticated user
 * has access to the requested organisation.
 *
 * Services that use org-level access control provide ONE bean of this type.
 * Services that don't need org filtering simply don't register a bean —
 * JwtFilter skips org validation automatically.
 *
 * The validator receives the full AuthClaims so it can access any field
 * (userId, role, etc.) without needing a separate lookup.
 *
 * Example:
 *
 *   @Component
 *   public class MyOrgAccessValidator implements OrgAccessValidator {
 *       public void validate(AuthClaims claims, Long orgId) {
 *           CrmAuthClaims c = (CrmAuthClaims) claims;
 *           if (!membershipRepo.existsByUserIdAndOrgId(c.userId(), orgId)) {
 *               throw new AccessDeniedException("User not in org");
 *           }
 *       }
 *   }
 */
@FunctionalInterface
public interface GenericOrgAccessValidator {
    /**
     * @throws RuntimeException if access is denied — JwtFilter catches it and returns 403
     */
    void validate(AuthClaims claims, Long orgId);
}
