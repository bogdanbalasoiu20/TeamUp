package com.teamup.teamUp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user"})
public class Notification {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notif_user"))
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String title;
    private String body;

    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(name = "is_seen",nullable = false)
    @Builder.Default
    private Boolean isSeen = false;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false,updatable = false)
    private Instant createdAt;

    @Column(name = "seen_at")
    private Instant seenAt;

}
