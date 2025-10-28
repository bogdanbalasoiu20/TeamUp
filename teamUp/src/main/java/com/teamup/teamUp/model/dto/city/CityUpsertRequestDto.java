package com.teamup.teamUp.model.dto.city;

import jakarta.validation.constraints.NotBlank;

public record CityUpsertRequestDto(
        @NotBlank String name,
        @NotBlank String slug,
        Double centerLat, Double centerLng,
        Double minLat, Double minLng, Double maxLat, Double maxLng,
        String countryCode
) {}
