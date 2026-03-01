package com.teamup.teamUp.model.dto.rating.player;

import com.teamup.teamUp.model.enums.Position;

import java.util.UUID;

//jucatorul care trebuie evaluat. se va folosi o lista
public record PlayerToRateDto(
        UUID userID,
        String username,
        Position position
) {
}
