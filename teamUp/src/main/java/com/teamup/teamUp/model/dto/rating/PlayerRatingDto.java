package com.teamup.teamUp.model.dto.rating;

import java.util.UUID;

public record PlayerRatingDto(
        UUID ratedUserId,
        int pace,
        int shooting,
        int passing,
        int dribbling,
        int defending,
        int physical,
        String comment

) {
}
