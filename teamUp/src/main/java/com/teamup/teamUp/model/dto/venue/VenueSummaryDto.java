package com.teamup.teamUp.model.dto.venue;

import java.util.UUID;

public record VenueSummaryDto(
        UUID id,
        String name,
        String city,
        Double lat,
        Double lng
) {
}
