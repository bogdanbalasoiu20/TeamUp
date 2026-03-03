package com.teamup.teamUp.model.entity;

import com.teamup.teamUp.model.id.TournamentMatchParticipantId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tournament_match_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentMatchParticipant {

    @EmbeddedId
    private TournamentMatchParticipantId id;

    @ManyToOne
    @MapsId("matchId")
    @JoinColumn(name = "match_id")
    private TournamentMatch match;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
}
