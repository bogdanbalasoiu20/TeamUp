package com.teamup.teamUp.model.dto.friend;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record FriendRequestActionDto(
        @NotNull
        @JsonProperty("accept")
        Boolean accept
) {
}
