package com.steve.corebanking;
import com.steve.corebanking.auth.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role tellerRole;

    @BeforeEach
    public void setUp() {
        tellerRole = new Role();
        tellerRole.setName("ROLE_TELLER");
        tellerRole.setId(1L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("john.doe");
        testUser.setPassword("plainPassword");
        testUser.setRoles(new HashSet<>());
    }

    // Test 1: Successful Registration
    @Test
    public void register_ShouldSuccessfullyRegisterUser_WhenValidInput() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(roleRepository.findByName("ROLE_TELLER")).thenReturn(Optional.of(tellerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L); // Simulate DB save
            return savedUser;
        });

        // Act
        User result = authService.register(testUser);

        // Assert
        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getRoles().contains(tellerRole));
        assertEquals(1, result.getRoles().size());

        // Verify interactions
        verify(userRepository).existsByUsername("john.doe");
        verify(roleRepository).findByName("ROLE_TELLER");
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(any(User.class));
    }

    // Test 2: Registration with Duplicate Username
    @Test
    public void register_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authService.register(testUser)
        );

        assertEquals("Username already taken", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // Test 3: Registration with Missing Role
    @Test
    public void register_ShouldThrowException_WhenRoleNotFound() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(roleRepository.findByName("ROLE_TELLER")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authService.register(testUser)
        );

        assertEquals("ROLE_TELLER not found", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // Test 4: Registration Clears Existing Roles
    @Test
    public void register_ShouldClearExistingRoles_BeforeAddingTellerRole() {
        // Arrange
        Role existingRole = new Role();
        existingRole.setName("ROLE_ADMIN");
        testUser.getRoles().add(existingRole);

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(roleRepository.findByName("ROLE_TELLER")).thenReturn(Optional.of(tellerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = authService.register(testUser);

        // Assert
        assertTrue(result.getRoles().contains(tellerRole));
        assertFalse(result.getRoles().contains(existingRole));
        assertEquals(1, result.getRoles().size());
    }

    // Test 5: Find By Username - User Exists
    @Test
    public void findByUsername_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findByUsername("john.doe"))
                .thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = authService.findByUsername("john.doe");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("john.doe", result.get().getUsername());
        verify(userRepository).findByUsername("john.doe");
    }

    // Test 6: Find By Username - User Doesn't Exist
    @Test
    public void findByUsername_ShouldReturnEmptyOptional_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent"))
                .thenReturn(Optional.empty());

        // Act
        Optional<User> result = authService.findByUsername("nonexistent");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername("nonexistent");
    }

    // Test 7: Get Password Encoder
    @Test
    public void getPasswordEncoder_ShouldReturnPasswordEncoder() {
        // Act
        PasswordEncoder result = authService.getPasswordEncoder();

        // Assert
        assertNotNull(result);
        assertEquals(passwordEncoder, result);
    }

    // Test 8: Registration with Null User
    @Test
    public void register_ShouldThrowNullPointerException_WhenUserIsNull() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> authService.register(null));
    }

    // Test 9: Registration with Null Username
    @Test
    public void register_ShouldHandleNullUsername_Gracefully() {
        // Arrange
        testUser.setUsername(null);
        when(userRepository.existsByUsername(null)).thenReturn(false);
        when(roleRepository.findByName("ROLE_TELLER")).thenReturn(Optional.of(tellerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Act & Assert
        assertDoesNotThrow(() -> authService.register(testUser));
    }

    // Test 10: Password Encoding Verification
    @Test
    public void register_ShouldEncodePassword_BeforeSaving() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(roleRepository.findByName("ROLE_TELLER")).thenReturn(Optional.of(tellerRole));
        when(passwordEncoder.encode("plainPassword")).thenReturn("encoded123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        // Act
        User result = authService.register(testUser);

        // Assert
        assertEquals("encoded123", result.getPassword());
        verify(passwordEncoder).encode("plainPassword");
    }
}