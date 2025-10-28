package com.teamup.teamUp.model.dto.city;

import java.util.UUID;

public record CityDto(
        UUID id, String name, String slug,
        Double centerLat, Double centerLng,
        Double minLat, Double minLng, Double maxLat, Double maxLng,
        String countryCode
) {}
