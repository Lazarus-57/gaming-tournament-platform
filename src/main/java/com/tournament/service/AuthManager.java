package com.tournament.service;

import com.tournament.model.User;
import com.tournament.model.enums.AccountState;
import com.tournament.model.enums.RoleType;
import com.tournament.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthManager {

    // Singleton Pattern: single auth manager instance per application.
    private static volatile AuthManager instance;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final RoleProxy roleProxy;

    public AuthManager(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       VerificationService verificationService,
                       RoleProxy roleProxy) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.roleProxy = roleProxy;
        instance = this;
    }

    public static AuthManager getInstance() {
        return instance;
    }

    // GRASP Controller: coordinates authentication workflows.
    public String register(User user) {
        user.setAccountState(AccountState.REGISTERED);
        userRepository.save(user);
        return verificationService.issueEmailToken(user.getUserId());
    }

    public void verifyEmail(Integer userId, String token) {
        if (!verificationService.verifyEmailToken(userId, token)) {
            throw new IllegalArgumentException("Invalid verification token");
        }
        User user = getUser(userId);
        user.setAccountState(AccountState.VERIFIED);
        user.setAccountState(AccountState.ACTIVE);
        userRepository.save(user);
    }

    public AuthSession login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getAccountState() == AccountState.SUSPENDED) {
            throw new SecurityException("User account is suspended");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new SecurityException("Invalid credentials");
        }

        if (user.getAccountState() == AccountState.REGISTERED || user.getAccountState() == AccountState.NEW) {
            throw new SecurityException("Email verification required");
        }

        if (user.isTwoFactorEnabled()) {
            verificationService.issueOtp(user.getUserId());
            return new AuthSession(user.getUserId(), user.getRole(), user.getAccountState(), true);
        }

        user.setAccountState(AccountState.ACTIVE);
        userRepository.save(user);
        return new AuthSession(user.getUserId(), user.getRole(), user.getAccountState(), false);
    }

    public AuthSession verifyOtp(Integer userId, String otp) {
        if (!verificationService.verifyOtp(userId, otp)) {
            throw new SecurityException("Invalid OTP");
        }
        User user = getUser(userId);
        user.setAccountState(AccountState.ACTIVE);
        userRepository.save(user);
        return new AuthSession(user.getUserId(), user.getRole(), user.getAccountState(), false);
    }

    public void logout(Integer userId) {
        User user = getUser(userId);
        user.setAccountState(AccountState.LOGGED_OUT);
        userRepository.save(user);
    }

    public void assertRole(User user, RoleType... allowed) {
        roleProxy.assertHasRole(user, allowed);
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
