package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.TournamentMatch;
import com.teamup.teamUp.model.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TournamentMatchRepository extends JpaRepository<TournamentMatch, UUID> {
    List<TournamentMatch> findByTournamentId(UUID tournamentId);
    List<TournamentMatch> findByTournamentIdAndStatus(UUID tournamentId, MatchStatus status);
    List<TournamentMatch> findByTournamentIdOrderByMatchDayAscIdAsc(UUID tournamentId);

    @Query("""
       SELECT m FROM TournamentMatch m
       WHERE m.homeTeam.id = :teamId
          OR m.awayTeam.id = :teamId
       """)
    List<TournamentMatch> findAllByTeamId(UUID teamId);
}
