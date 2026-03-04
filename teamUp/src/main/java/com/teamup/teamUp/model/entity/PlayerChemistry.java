package com.teamup.teamUp.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "player_chemistry",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_a", "user_b"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerChemistry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_a", nullable = false)
    private UUID userA;

    @Column(name = "user_b", nullable = false)
    private UUID userB;

    @Column(name = "chemistry_score", nullable = false)
    private int chemistryScore;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
