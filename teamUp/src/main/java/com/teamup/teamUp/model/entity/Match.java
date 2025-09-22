package com.teamup.teamUp.model.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Match {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "creator_user_id",nullable = false, foreignKey = @ForeignKey(name = "fk_match_creator"))
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "venue_id", nullable = false, foreignKey = @ForeignKey(name = "fk_match_venue"))
    private Venue venue;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "duration_min")
    private Integer durationMinutes;

    @Column(name = "max_players", nullable = false)
    private Integer maxPlayers;

    private String title;
    private String notes;

    @Builder.Default
    @Column(nullable = false)
    private String status = "OPEN";

    @Column(name = "total_price", precision = 10, scale =2)
    private BigDecimal totalPrice;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false,updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    private void validate(){
        if(maxPlayers == null || maxPlayers<=0)
            throw new IllegalArgumentException("The number of players must be greater than 0");
        if(durationMinutes != null && durationMinutes<=0)
            throw new IllegalArgumentException("The duration minutes must be greater than 0");
        if(status != null && !status.matches("OPEN|CANCELED|DONE|FULL"))
            throw new IllegalArgumentException("Invalid status: "  + status);
    }
}
