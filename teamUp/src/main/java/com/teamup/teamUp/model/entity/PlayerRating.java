package com.teamup.teamUp.model.entity;

import com.teamup.teamUp.model.id.PlayerRatingId;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "player_ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude ={"match", "raterUser", "ratedUser"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

//evaluarea primita de jucatorul X de la jucatorul Y in meciul Z
public class PlayerRating {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private PlayerRatingId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("matchId")
    @JoinColumn(name = "match_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pr_match"))
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("raterUserId")
    @JoinColumn(name = "rater_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pr_rater"))
    private User raterUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("ratedUserId")
    @JoinColumn(name = "rated_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pr_rated"))
    private User ratedUser;

    @Min(0)
    @Max(99)
    private Short pace;

    @Min(0)
    @Max(99)
    private Short shooting;

    @Min(0)
    @Max(99)
    private Short passing;

    @Min(0)
    @Max(99)
    private Short defending;

    @Min(0)
    @Max(99)
    private Short dribbling;

    @Min(0)
    @Max(99)
    private Short physical;

    @Min(0)
    @Max(99)
    private Short gkDiving;

    @Min(0)
    @Max(99)
    private Short gkHandling;

    @Min(0)
    @Max(99)
    private Short gkKicking;

    @Min(0)
    @Max(99)
    private Short gkReflexes;

    @Min(0)
    @Max(99)
    private Short gkSpeed;

    @Min(0)
    @Max(99)
    private Short gkPositioning;


    @Column(length = 500)
    private String comment;

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
