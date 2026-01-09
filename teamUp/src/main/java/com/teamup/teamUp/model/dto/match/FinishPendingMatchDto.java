package com.teamup.teamUp.model.dto.match;

import java.time.Instant;
import java.util.UUID;

public record FinishPendingMatchDto(
        UUID id,
        Instant startsAt,
        Integer durationMinutes
) {}
