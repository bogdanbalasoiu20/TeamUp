package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.Tournament;
import com.teamup.teamUp.model.enums.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TournamentRepository extends JpaRepository<Tournament, UUID> {
    List<Tournament> findByStatus(TournamentStatus status);
}
