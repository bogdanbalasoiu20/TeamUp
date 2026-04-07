package com.teamup.teamUp.model.dto.card;

import com.teamup.teamUp.model.enums.EventType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PlayerCardHistoryPointDto {

    private EventType eventType;
    private UUID contextId;
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
