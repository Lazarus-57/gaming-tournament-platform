package com.tournament.service;

import com.tournament.model.User;
import com.tournament.model.enums.AccountState;
import com.tournament.model.enums.RoleType;
import com.tournament.repository.UserRepository;
import com.tournament.service.factory.UserFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserFactory userFactory;

    public UserService(UserRepository userRepository, UserFactory userFactory) {
        this.userRepository = userRepository;
        this.userFactory = userFactory;
    }

    public User registerUser(RoleType role, String name, String email, String rawPassword, String extra) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = userFactory.createUser(role, name, email, rawPassword, extra);
        // Local demo default: activate immediately so users can log in right after registration.
        user.setAccountState(AccountState.ACTIVE);
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public void updateUserStatus(Integer userId, AccountState status) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setAccountState(status);
        userRepository.save(user);
    }

    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
    }

    public long getUserCount() {
        return userRepository.count();
    }
}
