package com.krunish.common.security.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionChecker permissionChecker;

    public PermissionAspect(PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }

    @Before("@annotation(permission)")
    public void check(RequiresPermission permission) {
        permissionChecker.check(permission.value());
    }
}

/*
    use Inside product-service:
    @Component
    @RequiredArgsConstructor
    public class OrgPermissionChecker implements PermissionChecker {

        private final PermissionFeignClient client;

        @Override
        public void check(String permission) {
            client.check(permission);
        }
    }

    @FeignClient(name = "org-service")
    public interface PermissionFeignClient {

        @PostMapping("/internal/org/permission/check")
        void check(@RequestParam String permission);
    }

    common-service
   ├── RequiresPermission
   ├── PermissionAspect
   └── PermissionChecker (interface only)

    product-service
       ├── PermissionFeignClient (real HTTP call)
       └── OrgPermissionChecker implements PermissionChecker

What Happens at Runtime?
    Spring sees:
    PermissionAspect needs PermissionChecker
    Product-service provides OrgPermissionChecker bean
    It injects it automatically
    So Aspect works without common knowing anything about org-service.
*/