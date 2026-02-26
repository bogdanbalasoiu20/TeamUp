package com.teamup.teamUp.model.dto.team;

import java.time.LocalDateTime;
import java.util.UUID;

public record TeamResponseDto(
        UUID id,
        String name,
        UUID captainId,
        String captainUsername,
        Double teamRating,
        Double teamChemistry,
        int membersCount,
        LocalDateTime createdAt
) {}

