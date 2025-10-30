package com.teamup.teamUp.model.dto.matchParticipant;

import jakarta.validation.constraints.Size;

public record JoinRequestDto(
        @Size(max=150)
        String message,
        Boolean bringsBall
) {
}
