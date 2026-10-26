package com.example.trackbill.AIBackend.util;

import com.example.trackbill.AIBackend.exception.UnauthorizedException;
import com.example.trackbill.AIBackend.model.TractorOwner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public static TractorOwner getCurrentOwner() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Unauthorized: No authenticated user session found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof TractorOwner) {
            return (TractorOwner) principal;
        }

        throw new UnauthorizedException("Unauthorized: Invalid authentication credentials");
    }

    public static Long getCurrentOwnerId() {
        return getCurrentOwner().getId();
    }
}
