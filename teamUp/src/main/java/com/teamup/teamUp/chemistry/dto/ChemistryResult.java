package com.teamup.teamUp.chemistry.dto;

import com.teamup.teamUp.model.enums.PlayerArchetype;

import java.util.List;

//rezultatul final, ceea ce trimit catre frontend
public record ChemistryResult(
        int score,  //scorul chemistry-ului (interval [0,99])
        double similarity, //scorul AI (interval [0,1])
        List<ChemistryReasons> reasons, //explicatia umana a scorului
        PlayerArchetype yourRole,
        PlayerArchetype otherRole
) {
}
