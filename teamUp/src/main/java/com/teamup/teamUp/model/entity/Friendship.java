package com.teamup.teamUp.model.entity;

import com.teamup.teamUp.model.id.FriendshipId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"userA","userB"})
@Entity
@Table(name = "friendships")
public class Friendship {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private FriendshipId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userA")
    @JoinColumn(name = "user_a", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userB")
    @JoinColumn(name = "user_b", nullable = false)
    private User userB;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Friendship(User userA, User userB) {
        this.userA = userA;
        this.userB = userB;
    }

    @PrePersist
    private void onCreate() {

        if (userA == null || userB == null)
            throw new IllegalArgumentException("Both users must be provided");

        if (userA.getId().equals(userB.getId()))
            throw new IllegalArgumentException("A user cannot befriend themselves");

        this.id = new FriendshipId(userA.getId(), userB.getId());
    }

}


