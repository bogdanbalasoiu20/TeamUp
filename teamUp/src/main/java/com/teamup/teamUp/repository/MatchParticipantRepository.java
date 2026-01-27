package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.dto.matchParticipant.ParticipantDto;
import com.teamup.teamUp.model.entity.MatchParticipant;
import com.teamup.teamUp.model.enums.MatchParticipantStatus;
import com.teamup.teamUp.model.id.MatchParticipantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, MatchParticipantId> {
    long countById_MatchId(UUID matchId);
    boolean existsById_MatchIdAndId_UserId(UUID matchId, UUID userId);

    @Query("select mp.user.id from MatchParticipant mp where mp.match.id = :matchId")
    List<UUID> findParticipantsIDs(UUID matchId);

    @Modifying
    @Query("delete from MatchParticipant mp where mp.match.id = :matchId and mp.user.id = :userId")
    int deleteByMatchIdAndUserId(UUID matchId, UUID userId);

    Optional<MatchParticipant> findById_MatchIdAndId_UserId(UUID matchId, UUID userId);

    long countById_MatchIdAndStatus(UUID matchId, MatchParticipantStatus status);


    @Query(value = """
   select new com.teamup.teamUp.model.dto.matchParticipant.ParticipantDto(
     mp.user.id,
     mp.user.username,
     mp.status,
     mp.bringsBall,
     mp.createdAt
   )
   from MatchParticipant mp
   where mp.id.matchId = :matchId
     and (CAST(:status AS string) IS NULL OR mp.status = :status)
   order by mp.createdAt asc
""",
            countQuery = """
   select count(mp)
   from MatchParticipant mp
   where mp.id.matchId = :matchId
     and (:status is null or mp.status = :status)
""")
    Page<ParticipantDto> findParticipants(UUID matchId, MatchParticipantStatus status, Pageable pageable);

    boolean existsById_MatchIdAndId_UserIdAndStatus(UUID matchId, UUID userId, MatchParticipantStatus status);

    List<MatchParticipant> findAllById_MatchId(UUID matchId);

    List<MatchParticipant> findAllById_MatchIdAndStatus(UUID matchId, MatchParticipantStatus status);

    @Query("""
    select mp.user.id
    from MatchParticipant mp
    where mp.match.id = :matchId
      and mp.status = :status
""")
    Set<UUID> findUserIdsByMatchAndStatus(
            @Param("matchId") UUID matchId,
            @Param("status") MatchParticipantStatus status
    );


    int countByUser_Id(UUID userId);

}
