package com.teamup.teamUp.model.dto.user;

import com.teamup.teamUp.model.enums.Position;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserProfileResponseDto(
        UUID id,
        String username,
        String email,
        LocalDate birthday,
        String phoneNumber,
        Position position,
        String city,
        String description,
        String rank,
        String photoUrl,
        Instant createdAt
) {
}
