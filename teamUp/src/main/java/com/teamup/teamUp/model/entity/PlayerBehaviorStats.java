package com.teamup.teamUp.model.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "player_behavior_stats",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_behavior_user",
                columnNames = "user_id"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerBehaviorStats {
    @Id
    @GeneratedValue
    @Column ( columnDefinition = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_behavior_user"))
    private User user;

    @Column (nullable = false)
    @Min(0)
    @Max(99)
    private double fairPlay;

    @Column (nullable = false)
    @Min(0)
    @Max(99)
    private double competitiveness;

    @Column(nullable = false)
    @Min(0)
    @Max(99)
    private double communication;

    @Column (nullable = false)
    @Min(0)
    @Max(99)
    private double fun;

    @Column (nullable = false)
    @Min(0)
    @Max(99)
    private double adaptability;

    @Column (nullable = false)
    @Min(0)
    @Max(99)
    private double reliability;

    @Column (nullable = false)
    private int feedbackCount;

    @CreationTimestamp
    @Column (updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
