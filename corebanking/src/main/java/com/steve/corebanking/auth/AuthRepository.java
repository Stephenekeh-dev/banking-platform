package com.steve.corebanking.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);


    @Query("""
        SELECT COUNT(u) > 0
        FROM User u
        JOIN u.roles r
        WHERE r.name = :roleName
    """)
    boolean existsUserWithRole(@Param("roleName") String roleName);
}