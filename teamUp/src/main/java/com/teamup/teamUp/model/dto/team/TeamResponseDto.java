package com.teamup.teamUp.model.dto.team;

import com.teamup.teamUp.chemistry.dto.TeamChemistryDto;
import com.teamup.teamUp.model.dto.rating.team.TeamRatingDto;

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
        TeamRatingDto rating
) {}

