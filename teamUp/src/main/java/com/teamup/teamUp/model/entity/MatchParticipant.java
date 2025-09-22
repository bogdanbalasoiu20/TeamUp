package com.teamup.teamUp.model.entity;

import com.teamup.teamUp.model.id.MatchParticipantId;
import com.teamup.teamUp.model.enums.MatchParticipantStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name= "match_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"match","user"})
public class MatchParticipant {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private MatchParticipantId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("matchId")
    @JoinColumn(name = "match_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mp_match"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mp_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MatchParticipantStatus status = MatchParticipantStatus.REQUESTED;

    private String message;

    @Builder.Default
    @Column(name = "brings_ball", nullable = false)
    private Boolean bringsBall = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    @PrePersist
    @PreUpdate
    private void beforeSave() {
        if (id == null && match != null && user != null) {
            id = MatchParticipantId.builder()
                    .matchId(match.getId())
                    .userId(user.getId())
                    .build();
        }

        if (status == null) {
            throw new IllegalArgumentException("status cannot be null");
        }
    }
}
