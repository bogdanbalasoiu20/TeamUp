package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.model.entity.PlayerBehaviorStats;

public interface PlayerSimilarityService {
    // Am schimbat numele in 'calculate' pentru a sugera ca e matematica pura, nu DB lookup
    double calculate(PlayerBehaviorStats behaviorA, PlayerBehaviorStats behaviorB);
}