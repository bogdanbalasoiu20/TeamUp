package com.teamup.teamUp.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "player_card_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

//arata ratingurile de pe cardul actual al jucatorului(dupa feedbackul din ultimul meci)
public class PlayerCardStats {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Min(0)
    @Max(99)
    private Double pace;

    @Min(0)
    @Max(99)
    private Double shooting;

    @Min(0)
    @Max(99)
    private Double passing;

    @Min(0)
    @Max(99)
    private Double defending;

    @Min(0)
    @Max(99)
    private Double dribbling;

    @Min(0)
    @Max(99)
    private Double physical;

    @Min(0)
    @Max(99)
    private Double gkDiving;

    @Min(0)
    @Max(99)
    private Double gkHandling;

    @Min(0)
    @Max(99)
    private Double gkKicking;

    @Min(0)
    @Max(99)
    private Double gkReflexes;

    @Min(0)
    @Max(99)
    private Double gkSpeed;

    @Min(0)
    @Max(99)
    private Double gkPositioning;

    @Min(0) @Max(99)
    @Column(name = "overall_rating", nullable = false)
    private Double overallRating;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;
}
