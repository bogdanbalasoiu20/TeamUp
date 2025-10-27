package com.teamup.teamUp.model.dto.user;

import com.teamup.teamUp.model.enums.Position;

import java.util.UUID;

public record UserSummaryDto(
        UUID id,
        String username,
        Position position,
        String rank
) {
}
