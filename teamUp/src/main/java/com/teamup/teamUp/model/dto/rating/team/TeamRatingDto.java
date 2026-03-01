package com.teamup.teamUp.model.dto.rating.team;

public record TeamRatingDto(
        int attack,
        int midfield,
        int defense,
        double overall
) {
}
