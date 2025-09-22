package com.teamup.teamUp.model.id;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;


@Embeddable
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@EqualsAndHashCode
public class FriendshipId implements Serializable {
    private UUID userA;
    private UUID userB;
}
