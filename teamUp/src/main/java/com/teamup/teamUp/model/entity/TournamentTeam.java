package com.teamup.teamUp.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tournament_teams",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tournament_id", "team_id"}))
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TournamentTeam {

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
    private LocalDateTime joinedAt = LocalDateTime.now();

}
