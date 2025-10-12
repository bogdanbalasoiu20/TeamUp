package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);
    Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username,String email);
    Optional<User> findByUsernameIgnoreCaseAndDeletedFalse(String username);
}
