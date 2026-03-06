package com.teamup.teamUp.chemistry.dto;

import java.util.UUID;

public record TeamChemistryLinkDto(
        UUID playerA,
        UUID playerB,
        int chemistry
) {}
