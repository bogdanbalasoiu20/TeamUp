package com.teamup.teamUp.model.id;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PlayerBehaviorRatingId implements Serializable {
    private UUID matchId;
    private UUID raterUserId;
    private UUID ratedUserId;
}