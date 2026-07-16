package com.studentmanagement.repository;

import com.studentmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find active user by username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a user exists with the given username.
     */
    boolean existsByUsername(String username);
}
