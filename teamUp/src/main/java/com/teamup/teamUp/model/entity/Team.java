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

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private int teamChemistry = 0;

    @Builder.Default
    @Column(nullable = false)
    private int overallRating = 0;

    @Builder.Default
    @Column(nullable = false)
    private int attackRating = 0;

    @Builder.Default
    @Column(nullable = false)
    private int midfieldRating = 0;

    @Builder.Default
    @Column(nullable = false)
    private int defenseRating = 0;

    private String badgeUrl;

}
