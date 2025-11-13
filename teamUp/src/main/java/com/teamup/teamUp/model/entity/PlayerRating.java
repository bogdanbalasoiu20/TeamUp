package com.teamup.teamUp.model.entity;

import com.teamup.teamUp.model.id.PlayerRatingId;
import jakarta.persistence.*;
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

    @Column(nullable = false)
    private short pace;

    @Column(nullable = false)
    private short shooting;

    @Column(nullable = false)
    private short passing;

    @Column(nullable = false)
    private short defending;

    @Column(nullable = false)
    private short dribbling;

    @Column(nullable = false)
    private short physical;

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
