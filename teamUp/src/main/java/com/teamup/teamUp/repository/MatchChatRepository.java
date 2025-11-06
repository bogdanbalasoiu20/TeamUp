package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.MatchChatMessage;
import com.teamup.teamUp.model.enums.MatchParticipantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface MatchChatRepository extends JpaRepository<MatchChatMessage, UUID> {
    @Query("""
select m from MatchChatMessage m
where m.match.id = :matchId
and(:after is null or m.createdAt>:after)
order by m.createdAt asc , m.id asc

""")
    Page<MatchChatMessage> findByMatchIdAfter(UUID matchId, Instant after, Pageable pageable);  //after este folosit pentru a-i spune serverului sa imi dea doar mesajele mai noi decat timestamp-ul X(adica: du-ma la primul mesaj necitit, nu incerca toata converstaia de la inceput)

    boolean existsById_MatchIdAndId_UserIdAndStatus(UUID matchId, UUID userId, MatchParticipantStatus status);

}
