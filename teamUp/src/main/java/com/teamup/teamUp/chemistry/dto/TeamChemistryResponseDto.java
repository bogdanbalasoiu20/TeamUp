package com.teamup.teamUp.chemistry.dto;

import java.util.List;

public record TeamChemistryResponseDto(
        int teamChemistry,
        List<TeamChemistryLinkDto> links
) {}