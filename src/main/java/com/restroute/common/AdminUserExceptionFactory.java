package com.restroute.common;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public final class AdminUserExceptionFactory {

    private AdminUserExceptionFactory() {}

    public static UsernameNotFoundException usernameNotFound(String username) {
        return new UsernameNotFoundException("Admin user not found: " + username);
    }
}
