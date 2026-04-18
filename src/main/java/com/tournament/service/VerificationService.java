package com.tournament.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationService {

    private final Map<Integer, String> emailTokens = new ConcurrentHashMap<>();
    private final Map<Integer, String> otpCodes = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    // SRP: verification logic isolated from authentication.
    public String issueEmailToken(Integer userId) {
        String token = UUID.randomUUID().toString();
        emailTokens.put(userId, token);
        return token;
    }

    public boolean verifyEmailToken(Integer userId, String token) {
        String stored = emailTokens.get(userId);
        return stored != null && stored.equals(token);
    }

    public String issueOtp(Integer userId) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        otpCodes.put(userId, otp);
        return otp;
    }

    public boolean verifyOtp(Integer userId, String otp) {
        String stored = otpCodes.get(userId);
        return stored != null && stored.equals(otp);
    }
}
