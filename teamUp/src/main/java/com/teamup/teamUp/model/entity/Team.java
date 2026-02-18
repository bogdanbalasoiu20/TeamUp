package com.teamup.teamUp.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "teams")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "captain_id")
    private User captain;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> members;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private int wins = 0;
    private int draws = 0;
    private int losses = 0;

    @Builder.Default
    private Double teamRating = 99.0;

    @Builder.Default
    private Double teamChemistry = 0.0;

}
