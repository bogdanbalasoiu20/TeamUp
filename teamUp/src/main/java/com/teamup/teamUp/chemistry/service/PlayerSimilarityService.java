package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.model.entity.PlayerBehaviorStats;

public interface PlayerSimilarityService {
    double calculate(PlayerBehaviorStats behaviorA, PlayerBehaviorStats behaviorB);
}