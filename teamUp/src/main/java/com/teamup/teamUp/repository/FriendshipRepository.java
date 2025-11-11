package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.Friendship;
import com.teamup.teamUp.model.id.FriendshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {
    @Query("""
        select f from Friendship f
        where f.userA.id = :userId or f.userB.id = :userId
    """)
    List<Friendship> findAllByUser(UUID userId);

    boolean existsByUserAIdAndUserBId(UUID userA, UUID userB);
}
