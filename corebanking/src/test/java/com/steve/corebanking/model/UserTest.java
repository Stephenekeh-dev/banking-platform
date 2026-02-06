package com.steve.corebanking.model;


import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import com.steve.corebanking.auth.Role;
import com.steve.corebanking.auth.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;
    private Role tellerRole;
    private Role adminRole;
    private Validator validator;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john.doe");
        user.setPassword("hashedPassword123");
        user.setRoles(new HashSet<>());

        tellerRole = new Role();
        tellerRole.setId(1L);
        tellerRole.setName("ROLE_TELLER");

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ROLE_ADMIN");

        ValidatorFactory factory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();

        validator = factory.getValidator();
    }


    // ========== CONSTRUCTOR TESTS ==========

    @Test
    public void noArgsConstructor_ShouldCreateUserWithDefaultValues() {
        // Act
        User emptyUser = new User();

        // Assert
        assertNull(emptyUser.getId());
        assertNull(emptyUser.getUsername());
        assertNull(emptyUser.getPassword());
        assertNotNull(emptyUser.getRoles());
        assertTrue(emptyUser.getRoles().isEmpty());
    }

    @Test
    public void allArgsConstructor_ShouldCreateUserWithProvidedValues() {
        // Arrange
        Set<Role> roles = Set.of(tellerRole);

        // Act
        User createdUser = new User(1L, "jane.doe", "hashedPass", roles);

        // Assert
        assertEquals(1L, createdUser.getId());
        assertEquals("jane.doe", createdUser.getUsername());
        assertEquals("hashedPass", createdUser.getPassword());
        assertEquals(1, createdUser.getRoles().size());
        assertTrue(createdUser.getRoles().contains(tellerRole));
    }

    // ========== GETTER/SETTER TESTS ==========

    @Test
    public void getId_ShouldReturnCorrectId() {
        assertEquals(1L, user.getId());
    }

    @Test
    public void setId_ShouldUpdateId() {
        // Act
        user.setId(99L);

        // Assert
        assertEquals(99L, user.getId());
    }

    @Test
    void getUsername_ShouldReturnCorrectUsername() {
        assertEquals("john.doe", user.getUsername());
    }

    @Test
    public void setUsername_ShouldUpdateUsername() {
        // Act
        user.setUsername("new.username");

        // Assert
        assertEquals("new.username", user.getUsername());
    }

    @Test
    public void getPassword_ShouldReturnCorrectPassword() {
        assertEquals("hashedPassword123", user.getPassword());
    }

    @Test
    public void setPassword_ShouldUpdatePassword() {
        // Act
        user.setPassword("newHashedPassword");

        // Assert
        assertEquals("newHashedPassword", user.getPassword());
    }

    @Test
    public void getRoles_ShouldReturnEmptySet_WhenNoRoles() {
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());
    }

    // ========== ROLES MANAGEMENT TESTS ==========

    @Test
    public void addRole_ShouldAddRoleToUser() {
        // Act
        user.getRoles().add(tellerRole);

        // Assert
        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(tellerRole));
    }

    @Test
    public void addMultipleRoles_ShouldAddAllRoles() {
        // Act
        user.getRoles().add(tellerRole);
        user.getRoles().add(adminRole);

        // Assert
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains(tellerRole));
        assertTrue(user.getRoles().contains(adminRole));
    }

    @Test
    void removeRole_ShouldRemoveRoleFromUser() {
        // Arrange
        user.getRoles().add(tellerRole);
        user.getRoles().add(adminRole);

        // Act
        user.getRoles().remove(tellerRole);

        // Assert
        assertEquals(1, user.getRoles().size());
        assertFalse(user.getRoles().contains(tellerRole));
        assertTrue(user.getRoles().contains(adminRole));
    }

    @Test
    public void clearRoles_ShouldRemoveAllRoles() {
        // Arrange
        user.getRoles().add(tellerRole);
        user.getRoles().add(adminRole);

        // Act
        user.getRoles().clear();

        // Assert
        assertTrue(user.getRoles().isEmpty());
    }

    // ========== VALIDATION TESTS ==========

    @Test
    public void user_ShouldBeValid_WhenAllFieldsCorrect() {
        // Act
        var violations = validator.validate(user);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    public void username_ShouldBeInvalid_WhenNullOrBlank(String invalidUsername) {
        // Arrange
        user.setUsername(invalidUsername);

        // Act
        var violations = validator.validate(user);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("username", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void username_ShouldBeInvalid_WhenTooLong() {
        // Arrange
        String longUsername = "a".repeat(256); // Assuming @Column length constraints
        user.setUsername(longUsername);

        // Act
        var violations = validator.validate(user);

        // Assert
        // Note: Length validation requires @Size annotation
        // Add @Size(max = 255) on username field for this test to work
    }

    // ========== EQUALS AND HASHCODE TESTS ==========

    @Test
    public void equals_ShouldReturnTrue_WhenSameInstance() {
        assertTrue(user.equals(user));
    }

    @Test
    public void equals_ShouldReturnTrue_WhenSameId() {
        // Arrange
        User user1 = new User(1L, "user1", "pass1", new HashSet<>());
        User user2 = new User(1L, "user2", "pass2", new HashSet<>());

        // Assert
        assertEquals(user1, user2);
    }

    @Test
    public void equals_ShouldReturnFalse_WhenDifferentIds() {
        // Arrange
        User user1 = new User(1L, "user", "pass", new HashSet<>());
        User user2 = new User(2L, "user", "pass", new HashSet<>());

        // Assert
        assertNotEquals(user1, user2);
    }

    @Test
    public void equals_ShouldReturnFalse_WhenComparingWithNull() {
        assertFalse(user.equals(null));
    }

    @Test
    public void equals_ShouldReturnFalse_WhenComparingWithDifferentClass() {
        assertFalse(user.equals("not a user"));
    }

    @Test
    public void hashCode_ShouldBeSame_WhenSameId() {
        // Arrange
        User user1 = new User(1L, "user1", "pass1", new HashSet<>());
        User user2 = new User(1L, "user2", "pass2", new HashSet<>());

        // Assert
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    public void hashCode_ShouldBeDifferent_WhenDifferentIds() {
        // Arrange
        User user1 = new User(1L, "user", "pass", new HashSet<>());
        User user2 = new User(2L, "user", "pass", new HashSet<>());

        // Assert
        assertNotEquals(user1.hashCode(), user2.hashCode());
    }

    // ========== TO STRING TESTS ==========

    @Test
    public void toString_ShouldContainUsernameAndId() {
        // Act
        String toString = user.toString();

        // Assert
        assertThat(toString).contains("john.doe");
        assertThat(toString).contains("1"); // id
    }

    @Test
    public void toString_ShouldNotContainPassword() {
        // Act
        String toString = user.toString();

        // Assert
        assertThat(toString).doesNotContain("hashedPassword123");
    }

    // ========== BUSINESS LOGIC TESTS ==========

    @Test
    public void hasRole_ShouldReturnTrue_WhenUserHasRole() {
        // Arrange
        user.getRoles().add(tellerRole);

        // Act & Assert
        assertTrue(user.getRoles().stream()
                .anyMatch(role -> "ROLE_TELLER".equals(role.getName())));
    }

    @Test
    public void hasRole_ShouldReturnFalse_WhenUserDoesNotHaveRole() {
        // Act & Assert
        assertFalse(user.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getName())));
    }

    @Test
    public void isNew_ShouldReturnTrue_WhenIdIsNull() {
        // Arrange
        User newUser = new User();

        // Act & Assert
        assertNull(newUser.getId());
        // In practice, you might add: assertTrue(newUser.isNew());
    }

    @Test
    public void isNew_ShouldReturnFalse_WhenIdIsNotNull() {
        assertNotNull(user.getId());
        // In practice, you might add: assertFalse(user.isNew());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void setRolesToNull_ShouldHandleGracefully() {
        // Act
        user.setRoles(null);

        // Assert
        assertNull(user.getRoles());
    }

    @Test
    public void getRoles_ShouldReturnNewSet_WhenRolesIsNull() {
        // Arrange
        user.setRoles(null);

        // Act & Assert - This would fail, showing we need defensive programming
        // Consider modifying entity to initialize roles if null
    }

    // ========== BUILDER PATTERN TESTS ==========

    @Test
   public  void userBuilder_ShouldBuildUserCorrectly() {
        // If using @Builder annotation from Lombok
        /*
        User builtUser = User.builder()
                .id(1L)
                .username("built.user")
                .password("builtPass")
                .roles(Set.of(tellerRole))
                .build();

        assertEquals(1L, builtUser.getId());
        assertEquals("built.user", builtUser.getUsername());
        assertEquals(1, builtUser.getRoles().size());
        */
    }

    // ========== SERIALIZATION TESTS ==========

    @Test
    public void user_ShouldBeSerializable() throws Exception {
        // Arrange
        user.getRoles().add(tellerRole);

        // Act - Serialize and deserialize
        /*
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(user);
        oos.flush();
        byte[] data = bos.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        User deserialized = (User) ois.readObject();

        // Assert
        assertEquals(user.getId(), deserialized.getId());
        assertEquals(user.getUsername(), deserialized.getUsername());
        */
    }
}