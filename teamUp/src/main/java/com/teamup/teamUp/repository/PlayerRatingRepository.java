package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.PlayerRating;
import com.teamup.teamUp.model.id.PlayerRatingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerRatingRepository extends JpaRepository<PlayerRating, PlayerRatingId> {
    List<PlayerRating> findByIdMatchId(UUID matchId);

    boolean existsByIdMatchIdAndIdRaterUserIdAndIdRatedUserId(UUID matchId,UUID raterUserId,UUID ratedUserId);

    int countByRaterUser_Id(UUID userId);

    int countByRatedUser_Id(UUID userId);
}
