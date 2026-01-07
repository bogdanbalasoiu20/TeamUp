package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.PlayerCardStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayerCardStatsRepository extends JpaRepository<PlayerCardStats, UUID> {
}
