package com.teamup.teamUp.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "player_card_stats_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

//folosesc aceasta entitatea pentru statistici, pentru a captura cardul de dupa fiecare meci, sa se vada evolutia/invloutia
public class PlayerCardStatsHistory {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private Double pace;
    private Double shooting;
    private Double passing;
    private Double defending;
    private Double dribbling;
    private Double physical;
    private Double gkDiving;
    private Double gkHandling;
    private Double gkKicking;
    private Double gkReflexes;
    private Double gkSpeed;
    private Double gkPositioning;


    @Column(name = "overall_rating", nullable = false)
    private Double overallRating;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "match_id", nullable = false)
    private UUID matchId;
}

