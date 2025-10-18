package com.teamup.teamUp.model.dto.venue;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record VenueAdminUpdateRequestDto(
        String name,
        String address,
        String phoneNumber,
        String city,
        @DecimalMin(value = "-90", message = "Latitude must be >= -90")
        @DecimalMax(value = "90",  message = "Latitude must be <= 90")
        Double latitude,
        @DecimalMin(value = "-180", message = "Longitude must be >= -180")
        @DecimalMax(value = "180",  message = "Longitude must be <= 180")
        Double longitude
) {}
