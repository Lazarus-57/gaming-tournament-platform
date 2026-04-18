package com.tournament.service;

import com.tournament.model.enums.AccountState;
import com.tournament.model.enums.RoleType;

public class AuthSession {
    private final Integer userId;
    private final RoleType role;
    private final AccountState accountState;
    private final boolean otpRequired;

    public AuthSession(Integer userId, RoleType role, AccountState accountState, boolean otpRequired) {
        this.userId = userId;
        this.role = role;
        this.accountState = accountState;
        this.otpRequired = otpRequired;
    }

    public Integer getUserId() { return userId; }
    public RoleType getRole() { return role; }
    public AccountState getAccountState() { return accountState; }
    public boolean isOtpRequired() { return otpRequired; }
}
