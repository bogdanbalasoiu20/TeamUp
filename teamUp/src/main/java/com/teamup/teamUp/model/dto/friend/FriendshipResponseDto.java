package com.teamup.teamUp.model.dto.friend;

import java.time.Instant;
import java.util.UUID;

public record FriendshipResponseDto(
        UUID userId,
        String username,
        String city,
        Instant since
) {
}
