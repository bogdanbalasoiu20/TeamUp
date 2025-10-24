package com.teamup.teamUp.model.entity;


import com.teamup.teamUp.model.enums.MatchStatus;
import com.teamup.teamUp.model.enums.MatchVisibility;
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

    @Column(name = "ends_at", nullable = false, insertable = false, updatable = false)
    private Instant endsAt;

    @Column(name = "duration_min")
    private Integer durationMinutes;

    @Column(name = "max_players", nullable = false)
    private Integer maxPlayers;

    @Builder.Default
    @Column(name = "current_players", nullable = false)
    private Integer currentPlayers = 0;

    @Column(name = "join_deadline")
    private Instant joinDeadline;

    private String title;
    private String notes;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.OPEN;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchVisibility visibility =  MatchVisibility.PUBLIC;

    @Column(name = "total_price", precision = 10, scale =2)
    private BigDecimal totalPrice;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false,updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @PrePersist
    @PreUpdate
    private void validate(){
        if(maxPlayers == null || maxPlayers<=0)
            throw new IllegalArgumentException("The number of players must be greater than 0");
        if(durationMinutes != null && durationMinutes<=0)
            throw new IllegalArgumentException("The duration minutes must be greater than 0");
        if(startsAt == null)
            throw new IllegalArgumentException("The starting hour is required");

        if(endsAt!=null && !endsAt.isAfter(startsAt)){
            throw new IllegalArgumentException("The ending hour must be after the start hour");
        }
        if(currentPlayers == null){
            currentPlayers = 0;
        }
        if (currentPlayers < 0 || (maxPlayers != null && currentPlayers > maxPlayers))
            throw new IllegalArgumentException("currentPlayers must be between 0 and maxPlayers");
        if (joinDeadline != null && !joinDeadline.isBefore(startsAt))
            throw new IllegalArgumentException("joinDeadline must be before startsAt");
    }
}
