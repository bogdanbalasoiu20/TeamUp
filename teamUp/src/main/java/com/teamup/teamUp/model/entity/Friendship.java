package com.teamup.teamUp.model.entity;

import com.teamup.teamUp.model.id.FriendshipId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "friendships")
@Getter @Setter
@NoArgsConstructor
public class Friendship {

    @EmbeddedId
    private FriendshipId id = new FriendshipId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userA")
    @JoinColumn(name = "user_a", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userB")
    @JoinColumn(name = "user_b", nullable = false)
    private User userB;

    @CreationTimestamp
    private Instant createdAt;

    public Friendship(User userA, User userB) {
        this.userA = userA;
        this.userB = userB;
        this.id = new FriendshipId(userA.getId(), userB.getId());
    }
}



