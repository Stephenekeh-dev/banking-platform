package com.steve.corebanking.auth;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {

    private final AuthRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository  roleRepository;

    public AuthService(AuthRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository  roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public User register(User user) {

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalStateException("Username already taken");
        }
        Role tellerRole = roleRepository
                .findByName("ROLE_TELLER")
                .orElseThrow(() -> new IllegalStateException("ROLE_TELLER not found"));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.getRoles().clear();
        user.getRoles().add(tellerRole);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {

        return userRepository.findByUsername(username);
    }

    public PasswordEncoder getPasswordEncoder() {

        return passwordEncoder;
    }
}