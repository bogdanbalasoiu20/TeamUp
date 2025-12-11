package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.FriendRequest;
import com.teamup.teamUp.model.enums.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {
    boolean existsByRequesterIdAndAddresseeIdAndStatus(UUID requesterId, UUID addresseeId, FriendRequestStatus status);

    List<FriendRequest> findAllByAddresseeIdAndStatus(UUID addresseeId, FriendRequestStatus status);

    List<FriendRequest> findAllByRequesterIdAndStatus(UUID requesterId, FriendRequestStatus status);

    Optional<FriendRequest> findByRequesterIdAndAddresseeId(UUID requesterId, UUID addresseeId);

    @Query("""
        select fr from FriendRequest fr
        where (fr.requester.id = :user1 and fr.addressee.id = :user2)
           or (fr.requester.id = :user2 and fr.addressee.id = :user1)
    """)
    Optional<FriendRequest> findBetweenUsers(UUID user1, UUID user2);

    @Query("""
    SELECT fr FROM FriendRequest fr
    WHERE (
         (fr.requester.id = :a AND fr.addressee.id = :b)
      OR (fr.requester.id = :b AND fr.addressee.id = :a)
    )
    AND fr.status = 'PENDING'
""")
    Optional<FriendRequest> findPendingBetweenUsers(UUID a, UUID b);


    @Modifying
    @Query("""
    UPDATE FriendRequest fr
    SET fr.status = 'CANCELED'
    WHERE (fr.requester.id = :a AND fr.addressee.id = :b)
       OR (fr.requester.id = :b AND fr.addressee.id = :a)
""")
    void cancelAllBetween(UUID a, UUID b);


}
