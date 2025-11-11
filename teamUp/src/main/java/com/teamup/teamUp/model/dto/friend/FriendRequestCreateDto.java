package com.teamup.teamUp.model.dto.friend;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record FriendRequestCreateDto(
        @NotNull
        UUID addresseeId,

        @Size(max=200)
        String message
) {
}
