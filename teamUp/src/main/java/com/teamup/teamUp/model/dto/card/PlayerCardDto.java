package com.teamup.teamUp.model.dto.card;

import com.teamup.teamUp.model.enums.Position;

import java.util.Map;
import java.util.UUID;

public record PlayerCardDto(
        UUID userId,
        String name,
        Position position,
        int overall,
        String imageUrl,
        Map<String, Integer> stats
) {}
