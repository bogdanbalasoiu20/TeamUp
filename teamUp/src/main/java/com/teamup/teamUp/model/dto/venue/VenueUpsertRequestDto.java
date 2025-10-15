package com.teamup.teamUp.model.dto.venue;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record VenueUpsertRequestDto(
        @NotBlank
        String name,
        String address,
        String phoneNumber,
        String city,

        @DecimalMin("-90") @DecimalMax("90")
        Double latitude,

        @DecimalMin("-180") @DecimalMax("180")
        Double longitude,

        String osmType,
        Long osmId,
        Map<String,Object> tagsJson
) {
}
