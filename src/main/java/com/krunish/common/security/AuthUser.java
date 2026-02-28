package com.krunish.common.security;

public record AuthUser(
        Long userId,
        String email
) {}
