package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.model.enums.PlayerArchetype;

import java.util.UUID;

public interface UserRoleService {
    PlayerArchetype getRole(UUID userId);
}

