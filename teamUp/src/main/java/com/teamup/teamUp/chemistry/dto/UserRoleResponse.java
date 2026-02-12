package com.teamup.teamUp.chemistry.dto;

import com.teamup.teamUp.model.enums.PlayerArchetype;

public record UserRoleResponse(
        PlayerArchetype role
) {}

