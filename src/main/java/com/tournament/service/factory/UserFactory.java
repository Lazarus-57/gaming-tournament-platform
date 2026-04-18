package com.tournament.service.factory;

import com.tournament.model.*;
import com.tournament.model.enums.RoleType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Factory Pattern: Creates different types of User objects
 * based on the specified role type.
 */
@Component
public class UserFactory {

    private final PasswordEncoder passwordEncoder;

    public UserFactory(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(RoleType role, String name, String email, String rawPassword, String extra) {
        String encodedPassword = passwordEncoder.encode(rawPassword);

        return switch (role) {
            case PLAYER -> new Player(name, email, encodedPassword, extra);   // extra = gamerTag
            case ORGANIZER -> new Organizer(name, email, encodedPassword, extra); // extra = organizationName
            case ADMIN -> new Admin(name, email, encodedPassword, 1);
        };
    }
}
