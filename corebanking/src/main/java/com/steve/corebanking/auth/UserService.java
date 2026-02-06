package com.steve.corebanking.auth;

import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {

    private final AuthRepository authRepository;
    private final RoleRepository roleRepository;

    public UserService(AuthRepository authRepository,
                       RoleRepository roleRepository) {
        this.authRepository = authRepository;
        this.roleRepository = roleRepository;
    }

    public void changeUserRole(Long targetUserId,
                               String roleName,
                               Long currentAdminId) {

        //  ADMIN cannot change own role
        if (targetUserId.equals(currentAdminId)) {
            throw new AccessDeniedException("Admin cannot change own role");
        }

        User user = authRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role newRole = roleRepository.findByName("ROLE_" + roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // SINGLE ROLE SYSTEM
        user.getRoles().clear();
        user.getRoles().add(newRole);

        authRepository.save(user);
    }
}