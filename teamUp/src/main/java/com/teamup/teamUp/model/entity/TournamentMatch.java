package com.teamup.teamUp.model.entity;

import com.teamup.teamUp.model.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "tournament_matches")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TournamentMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    private Integer scoreHome;
    private Integer scoreAway;

    @ManyToOne
    @JoinColumn(name = "winner_team_id")
    private Team winner;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private MatchStatus status = MatchStatus.OPEN;

    private Integer matchDay;

}
