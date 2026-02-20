package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    List<TeamMember> findByTeamId(UUID teamId);
    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);
    List<TeamMember> findByUserUsernameIgnoreCase(String username);
}
