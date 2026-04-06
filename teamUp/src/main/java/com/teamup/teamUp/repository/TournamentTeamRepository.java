package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.TournamentTeam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, UUID> {
    boolean existsByTournamentIdAndTeamId(UUID tournamentId, UUID teamId);
    List<TournamentTeam> findByTournamentId(UUID tournamentId);

    @Query("""
        select distinct tt
        from TournamentTeam tt
        join fetch tt.tournament t
        join fetch t.venue
        join fetch tt.team team
        join team.members member
        where member.user.username = :username
          and t.startsAt > CURRENT_TIMESTAMP
          and t.status = 'OPEN'
    """)
    List<TournamentTeam> findUpcomingTournamentTeamsForUser(String username);

    @Query("""
        select tt.team.badgeUrl
        from TournamentTeam tt
        where tt.tournament.id = :tournamentId
    """)
    List<String> findBadgeUrls(UUID tournamentId, Pageable pageable);
}
