package com.tournament.service;

import com.tournament.model.User;
import com.tournament.model.enums.RoleType;
import org.springframework.stereotype.Component;

@Component
public class RoleProxy {

    // Proxy Pattern: access checks before protected operations.
    public void assertHasRole(User user, RoleType... allowed) {
        for (RoleType role : allowed) {
            if (user.getRole() == role) {
                return;
            }
        }
        throw new SecurityException("Access denied for role: " + user.getRole());
    }
}
