package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.TeamMember;
import com.teamup.teamUp.model.enums.SquadType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    List<TeamMember> findByTeamId(UUID teamId);
    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);
    List<TeamMember> findByUserUsernameIgnoreCase(String username);
    boolean existsByUserIdAndTeamId(UUID userId, UUID teamId);
    List<TeamMember> findByTeamIdAndSquadType(UUID teamId, SquadType squadType);
    Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId);
    Optional<TeamMember> findByTeamIdAndSquadTypeAndSlotIndex(UUID teamId, SquadType squadType, int slotIndex);
}
