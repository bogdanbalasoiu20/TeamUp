package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);
    Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username,String email);
    Optional<User> findByUsernameIgnoreCaseAndDeletedFalse(String username);

    @Query("""
    select u from User u
    where u.deleted = false
    and lower(u.username) like lower(concat('%', :query, '%'))
""")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

}
