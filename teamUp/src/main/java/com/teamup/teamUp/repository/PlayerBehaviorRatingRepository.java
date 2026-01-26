package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.PlayerBehaviorRating;
import com.teamup.teamUp.model.id.PlayerBehaviorRatingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayerBehaviorRatingRepository extends JpaRepository<PlayerBehaviorRating, PlayerBehaviorRatingId> {
    boolean existsByIdMatchIdAndIdRaterUserIdAndIdRatedUserId(
            UUID matchId,
            UUID raterUserId,
            UUID ratedUserId
    );
}

