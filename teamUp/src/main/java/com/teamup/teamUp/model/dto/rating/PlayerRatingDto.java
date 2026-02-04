package com.teamup.teamUp.model.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

//folosit cand un user evalueaza alt user
public record PlayerRatingDto(
        UUID ratedUserId,

        @Min(0) @Max(99)
        Short pace,

        @Min(0) @Max(99)
        Short shooting,

        @Min(0) @Max(99)
        Short passing,

        @Min(0) @Max(99)
        Short defending,

        @Min(0) @Max(99)
        Short dribbling,

        @Min(0) @Max(99)
        Short physical,

        @Min(0) @Max(99)
        Short gkDiving,

        @Min(0) @Max(99)
        Short gkHandling,

        @Min(0) @Max(99)
        Short gkKicking,

        @Min(0) @Max(99)
        Short gkReflexes,

        @Min(0) @Max(99)
        Short gkSpeed,

        @Min(0) @Max(99)
        Short gkPositioning,

        @Min(0) @Max(99)
        Short fairPlay,

        @Min(0) @Max(99)
        Short communication,

        @Min(0) @Max(99)
        Short fun,

        @Min(0) @Max(99)
        Short competitiveness,

        @Min(0) @Max(99)
        Short selfishness,

        @Min(0) @Max(99)
        Short aggressiveness

) {
}
