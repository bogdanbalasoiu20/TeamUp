package com.teamup.teamUp.model.entity;

import com.teamup.teamUp.model.id.FriendshipId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "friendships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"userA","userB"})
public class Friendship {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private FriendshipId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userA")
    @JoinColumn(name = "user_a", nullable = false, foreignKey = @ForeignKey(name = "fk_fs_a"))
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userB")
    @JoinColumn(name = "user_b", nullable = false, foreignKey = @ForeignKey(name = "fk_fs_b"))
    private User userB;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    private void onCreate() {

        if (userA == null || userB == null)
            throw new IllegalArgumentException("Both users must be provided");

        if (userA.getId().compareTo(userB.getId()) > 0) {
            User tmp = userA;
            userA = userB;
            userB = tmp;
        }

        if (id == null) {
            id = new FriendshipId(
                    userA.getId(),
                    userB.getId()
            );
        }
    }
}

