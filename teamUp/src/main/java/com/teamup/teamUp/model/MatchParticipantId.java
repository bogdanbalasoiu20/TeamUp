package com.teamup.teamUp.model;


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
    private UUID matchId;
    private UUID userId;
}
