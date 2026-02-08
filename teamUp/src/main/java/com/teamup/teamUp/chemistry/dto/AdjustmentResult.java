package com.teamup.teamUp.chemistry.dto;

import java.util.List;

public record AdjustmentResult(
        double adjustmentSimilarity, //scorul de similaritate obtinut dupa ajustari
        List<ChemistryReasons> reasons //explicatii pentru modificarea scorului
) {
}
