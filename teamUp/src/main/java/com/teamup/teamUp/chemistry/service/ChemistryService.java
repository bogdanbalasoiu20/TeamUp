package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.dto.ChemistryResult;
import java.util.UUID;

public interface ChemistryService {
    // Asta ramane la fel, primeste ID-urile de la Frontend/Controller
    ChemistryResult compute(UUID userA, UUID userB);
}