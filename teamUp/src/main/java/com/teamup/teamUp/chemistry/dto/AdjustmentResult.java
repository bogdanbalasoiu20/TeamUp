package com.teamup.teamUp.chemistry.dto;

import com.teamup.teamUp.model.enums.PlayerArchetype;

import java.util.List;

public record AdjustmentResult(
        double adjustmentSimilarity, //scorul de similaritate obtinut dupa ajustari
        List<ChemistryReasons> reasons, //explicatii pentru modificarea scorului
        PlayerArchetype roleA,
        PlayerArchetype roleB
) {
}
