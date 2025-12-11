package com.teamup.teamUp.model.dto.friend;

import java.util.UUID;

public record RelationStatusDto(
        boolean isFriend,
        boolean pendingSent,
        boolean pendingReceived,
        UUID requestId
) {}

