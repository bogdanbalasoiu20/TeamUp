package com.teamup.teamUp.model.dto.friend;

import jakarta.validation.constraints.NotNull;

public record FriendRequestActionDto(
        @NotNull
        Boolean accept
) {
}
