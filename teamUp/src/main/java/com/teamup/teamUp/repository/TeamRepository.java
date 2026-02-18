package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    Optional<Team> findByName(String name);
    boolean existsByName(String name);
}
