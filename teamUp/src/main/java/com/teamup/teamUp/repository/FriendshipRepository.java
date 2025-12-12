package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.Friendship;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.id.FriendshipId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {
    @Query("""
        select f from Friendship f
        where f.userA.id = :userId or f.userB.id = :userId
    """)
    Page<Friendship> findAllByUser(UUID userId, Pageable pageable);

    boolean existsByUserA_IdAndUserB_Id(UUID userA, UUID userB);

    @Modifying
    @Query("""
    delete from Friendship f
    where (f.userA.id = :userId and f.userB.id = :friendId)
       or (f.userA.id = :friendId and f.userB.id = :userId)
""")
    int deleteByUserIds(UUID userId, UUID friendId);


    @Query("""
select u from Friendship f
join User u on((f.userA.id = :userId and f.userB.id = u.id)
            or (f.userB.id = :userId and f.userA.id = u.id))
where (:search is null or lower(u.username) like (concat('%',:search,'%')))
""")
    List<User> searchAcceptedFriends(@Param("userId") UUID userId, @Param("search") String search);
}
