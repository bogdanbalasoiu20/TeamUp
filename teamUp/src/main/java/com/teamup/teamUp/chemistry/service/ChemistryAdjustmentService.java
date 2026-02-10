package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.dto.AdjustmentResult;
import com.teamup.teamUp.model.entity.PlayerBehaviorStats;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.enums.Position;

public interface ChemistryAdjustmentService {
    AdjustmentResult adjust(
            double baseSimilarity, // Rezultatul de la pasul 1
            PlayerCardStats statsA, Position posA, PlayerBehaviorStats behA,
            PlayerCardStats statsB, Position posB, PlayerBehaviorStats behB,
            int matchesTogether // Istoricul calculat deja
    );
}