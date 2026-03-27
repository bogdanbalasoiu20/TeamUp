package com.teamup.teamUp.model.dto.team;


import com.teamup.teamUp.chemistry.dto.TeamChemistryLinkDto;

import java.time.LocalDateTime;
import java.util.List;
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
        int defenseRating,
        List<TeamChemistryLinkDto> chemistryLinks,
        String badgeUrl
) {}

