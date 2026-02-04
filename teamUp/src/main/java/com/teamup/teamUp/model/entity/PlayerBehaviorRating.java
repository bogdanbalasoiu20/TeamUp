package com.teamup.teamUp.model.entity;

import com.teamup.teamUp.model.id.PlayerBehaviorRatingId;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "player_behavior_ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"match", "raterUser", "ratedUser"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

//evaluarea pentru comportament primita de userul X de la userul Y in meciul Z
public class PlayerBehaviorRating {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private PlayerBehaviorRatingId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("matchId")
    @JoinColumn(name = "match_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pbr_match"))
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("raterUserId")
    @JoinColumn(name = "rater_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pbr_rater"))
    private User raterUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("ratedUserId")
    @JoinColumn(name = "rated_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pbr_rated"))
    private User ratedUser;

    @Min(0) @Max(99)
    private Short fairPlay;

    @Min(0) @Max(99)
    private Short competitiveness;

    @Min(0) @Max(99)
    private Short communication;

    @Min(0) @Max(99)
    private Short fun;

    @Min(0) @Max(99)
    private Short selfishness;

    @Min(0) @Max(99)
    private Short aggressiveness;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    @PreUpdate
    private void validate() {
        if (raterUser != null && ratedUser != null && raterUser.getId().equals(ratedUser.getId())) {
            throw new IllegalArgumentException("User cannot rate themselves");
        }
    }
}
