package com.teamup.teamUp.model.dto.venue;

import com.teamup.teamUp.model.enums.VenueSource;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record VenueResponseDto(
        UUID id,
        String name,
        String address,
        String phoneNumber,
        String city,
        Double latitude,
        Double longitude,
        String osmType,
        Long osmId,
        Map<String,Object> tagsJson,
        String source,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
