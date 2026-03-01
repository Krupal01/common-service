package com.krunish.common.security;

import org.springframework.stereotype.Component;


public interface OrgAccessValidator {
    void validate(Long userId, Long orgId);
}

/*

How To Implement OrgAccessValidatorImpl
@Component
@RequiredArgsConstructor
public class OrgAccessValidatorImpl implements OrgAccessValidator {

    private final OrgClient orgClient;

    @Override
    public void validate(UUID userId, UUID orgId) {

        boolean allowed = orgClient.isUserInOrg(userId, orgId);

        if (!allowed) {
            throw new AppException("FORBIDDEN", "User does not belong to org");
        }
    }
}

🔥 Even Better (Performance Improvement)
Add caching:
Cache user-org mapping in Redis
Or cache inside memory for 5 minutes
Because org membership does not change frequently.

*/
