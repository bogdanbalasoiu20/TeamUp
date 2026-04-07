package com.teamup.teamUp.model.entity;

import com.teamup.teamUp.model.enums.EventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Column(name = "match_id")
    private UUID matchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private EventType eventType;

    @Column(name = "context_id")
    private UUID contextId;
}

