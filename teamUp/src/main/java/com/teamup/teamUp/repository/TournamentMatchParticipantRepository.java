package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.TournamentMatchParticipant;
import com.teamup.teamUp.model.enums.MatchStatus;
import com.teamup.teamUp.model.id.TournamentMatchParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TournamentMatchParticipantRepository extends JpaRepository<TournamentMatchParticipant, TournamentMatchParticipantId> {
    boolean existsByMatch_Id(UUID matchId);

    @Query("""
    SELECT COUNT(DISTINCT tmp1.match.id)
    FROM TournamentMatchParticipant tmp1,
         TournamentMatchParticipant tmp2
    WHERE tmp1.match.id = tmp2.match.id
      AND tmp1.team.id = tmp2.team.id
      AND tmp1.user.id = :userA
      AND tmp2.user.id = :userB
      AND tmp1.user.id <> tmp2.user.id
      AND tmp1.match.status = :status
""")
    int countTournamentMatchesTogether(@Param("userA") UUID userA, @Param("userB") UUID userB, @Param("status") MatchStatus status);


    @Query("""
    SELECT COUNT(DISTINCT tmp.match.id)
    FROM TournamentMatchParticipant tmp
    WHERE tmp.user.id = :userId
      AND tmp.match.status = :status
""")
    int countTournamentMatchesForUser(@Param("userId") UUID userId, @Param("status") MatchStatus status);
}
