package com.teamup.teamUp.chemistry.service;

import java.util.UUID;

public interface PlayerSimilarityService {
    double similarity(UUID userA, UUID userB);
}
