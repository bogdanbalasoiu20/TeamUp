package com.teamup.teamUp.model.dto.friend;

import java.util.UUID;

public record UserSearchResponseDto(
        UUID id,
        String username,
        String photoUrl,
        boolean isFriend,
        boolean pendingSent,
        boolean pendingReceived
) {}

