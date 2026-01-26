package com.teamup.teamUp.model.dto.card;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PlayerCardHistoryPointDto {

    private UUID matchId;
    private Instant recordedAt;

    private Double overallRating;

    private Double pace;
    private Double shooting;
    private Double passing;
    private Double dribbling;
    private Double defending;
    private Double physical;

    //null pentru jucator de camp
    private Double gkDiving;
    private Double gkHandling;
    private Double gkKicking;
    private Double gkReflexes;
    private Double gkSpeed;
    private Double gkPositioning;
}
