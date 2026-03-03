package com.teamup.teamUp.model.id;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class TournamentMatchParticipantId {
    private UUID matchId;
    private UUID userId;
}
