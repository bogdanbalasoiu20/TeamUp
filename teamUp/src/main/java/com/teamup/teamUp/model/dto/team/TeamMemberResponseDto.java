package com.teamup.teamUp.model.dto.team;

import com.teamup.teamUp.model.enums.SquadType;
import com.teamup.teamUp.model.enums.TeamRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record TeamMemberResponseDto(
        UUID userId,
        String username,
        TeamRole role,
        LocalDateTime joinedAt,
        SquadType squadType,
        Integer slotIndex
) {
}
