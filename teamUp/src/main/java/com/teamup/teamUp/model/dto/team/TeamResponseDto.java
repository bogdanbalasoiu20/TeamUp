package com.teamup.teamUp.model.dto.team;


import java.time.LocalDateTime;
import java.util.UUID;

public record TeamResponseDto(
        UUID id,
        String name,
        UUID captainId,
        String captainUsername,
        int teamChemistry,
        int membersCount,
        LocalDateTime createdAt,
        int overallRating,
        int attackRating,
        int midfieldRating,
        int defenseRating
) {}

