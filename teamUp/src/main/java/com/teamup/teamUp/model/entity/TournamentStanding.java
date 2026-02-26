package com.teamup.teamUp.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tournament_standings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tournament_id", "team_id"}))
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TournamentStanding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @Builder.Default
    private int played = 0;

    @Builder.Default
    private int wins = 0;

    @Builder.Default
    private int draws = 0;

    @Builder.Default
    private int losses = 0;

    @Builder.Default
    private int goalsFor = 0;

    @Builder.Default
    private int goalsAgainst = 0;

    @Builder.Default
    private int points = 0;

    private Integer finalPosition;

}

