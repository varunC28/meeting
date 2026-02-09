package com.cluely.security;

import org.springframework.security.core.context.SecurityContextHolder;
import java.util.UUID;

public class SecurityUtils {

    public static UUID getCurrentUserId() {

        String id = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return UUID.fromString(id);
    }
}
