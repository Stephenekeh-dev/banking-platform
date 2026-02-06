package com.steve.corebanking.auth;

import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private  final AuthRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public AuthController(AuthService authService, JwtUtil jwtUtil, AuthRepository userRepository,
                          RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserService userService) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.roleRepository =  roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        User savedUser = authService.register(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        return authService.findByUsername(user.getUsername())
                .filter(u -> authService.getPasswordEncoder().matches(user.getPassword(), u.getPassword()))
                .map(u -> ResponseEntity.ok(jwtUtil.generateToken(u)))
                .orElse(ResponseEntity.status(401).body("Invalid credentials"));
    }
    @PostMapping("/bootstrap-admin")
    public ResponseEntity<?> bootstrapAdmin(@RequestBody User user) {

        if (userRepository. existsUserWithRole("ROLE_ADMIN")) {
            return ResponseEntity.status(403).body("Admin already exists");
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.getRoles().add(adminRole);

        userRepository.save(user);

        return ResponseEntity.ok("Admin created");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/roles")
    public ResponseEntity<?> assignRole(
            @PathVariable Long id,
            @RequestParam String roleName) {

        User user = userRepository.findById(id).orElseThrow();
        Role role = roleRepository.findByName(roleName).orElseThrow();

        user.getRoles().add(role);
        userRepository.save(user);

        return ResponseEntity.ok("Role assigned");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long id,
                                            @RequestParam String roleName,
                                            Authentication authentication) {

        // Get the username from Authentication (safe)
        String adminUsername = authentication.getName();

        // Load admin from DB
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Delegate all role-change logic to the service
        userService.changeUserRole(id, roleName, admin.getId());

        return ResponseEntity.ok("User role updated");
    }

}