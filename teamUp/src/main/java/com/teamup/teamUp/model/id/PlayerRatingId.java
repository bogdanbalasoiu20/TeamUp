package com.teamup.teamUp.model.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerRatingId implements Serializable {

    @Column(name = "match_id", nullable = false, insertable = false, updatable = false)
    private UUID matchId;

    @Column(name = "rater_user_id", nullable = false, insertable = false, updatable = false)
    private UUID raterUserId;

    @Column(name = "rated_user_id", nullable = false, insertable = false, updatable = false)
    private UUID ratedUserId;
}
