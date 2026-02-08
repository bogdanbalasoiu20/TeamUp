package com.teamup.teamUp.chemistry.dto;

import com.teamup.teamUp.chemistry.ReasonType;

public record ChemistryReasons(
        String message,
        ReasonType type
) {}

