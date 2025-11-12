package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.UserStatsView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserStatsRepository extends JpaRepository<UserStatsView, UUID> {
    Optional<UserStatsView> findByUserId(UUID userId);
}
