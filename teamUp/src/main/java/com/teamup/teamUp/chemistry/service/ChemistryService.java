package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.dto.ChemistryResult;
import java.util.UUID;

public interface ChemistryService {
    ChemistryResult compute(UUID userA, UUID userB);
}