package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.TournamentMatchParticipant;
import com.teamup.teamUp.model.id.TournamentMatchParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TournamentMatchParticipantRepository extends JpaRepository<TournamentMatchParticipant, TournamentMatchParticipantId> {
    boolean existsByMatch_Id(UUID matchId);

    @Query("""
    SELECT COUNT(DISTINCT tmp1.match.id)
    FROM TournamentMatchParticipant tmp1
    JOIN TournamentMatchParticipant tmp2
      ON tmp1.match.id = tmp2.match.id
     AND tmp1.team.id = tmp2.team.id
    JOIN tmp1.match m
    WHERE tmp1.user.id = :userA
      AND tmp2.user.id = :userB
      AND m.status = com.teamup.teamUp.model.enums.MatchStatus.DONE
""")
    int countTournamentMatchesTogether(
            @Param("userA") UUID userA,
            @Param("userB") UUID userB
    );


    @Query("""
    SELECT COUNT(DISTINCT tmp.match.id)
    FROM TournamentMatchParticipant tmp
    JOIN tmp.match m
    WHERE tmp.user.id = :userId
      AND m.status = com.teamup.teamUp.model.enums.MatchStatus.DONE
""")
    int countTournamentMatchesForUser(@Param("userId") UUID userId);
}
