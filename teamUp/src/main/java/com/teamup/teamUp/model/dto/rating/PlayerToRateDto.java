package com.teamup.teamUp.model.dto.rating;

import com.teamup.teamUp.model.enums.Position;

import java.util.UUID;

public record PlayerToRateDto(
        UUID userID,
        String username,
        Position position
) {
}
