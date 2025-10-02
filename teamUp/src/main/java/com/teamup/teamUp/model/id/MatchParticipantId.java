package com.teamup.teamUp.model.id;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class MatchParticipantId implements Serializable {
    @Column(name = "match_id", nullable = false, insertable = false, updatable = false)
    private UUID matchId;

    @Column(name = "user_id", nullable = false, insertable = false, updatable = false)
    private UUID userId;
}
