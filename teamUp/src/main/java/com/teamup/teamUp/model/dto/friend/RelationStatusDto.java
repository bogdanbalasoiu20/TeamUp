package com.teamup.teamUp.model.dto.friend;

public record RelationStatusDto(
        boolean isFriend,
        boolean pendingSent,
        boolean pendingReceived
) {}

