package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.dto.AdjustmentResult;

import java.util.UUID;

public interface ChemistryAdjustmentService {
    AdjustmentResult adjust(UUID userA, UUID userB, double similarity);
}
