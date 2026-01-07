package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.dto.rating.PlayerRatingDto;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.model.entity.PlayerRating;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.Position;
import com.teamup.teamUp.model.id.PlayerRatingId;
import com.teamup.teamUp.repository.MatchParticipantRepository;
import com.teamup.teamUp.repository.PlayerRatingRepository;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PlayerRatingService {

    private final UserRepository userRepository;
    private final PlayerRatingRepository playerRatingRepository;
    private final MatchParticipantRepository matchParticipantRepository;

    public PlayerRatingService(UserRepository userRepository, PlayerRatingRepository playerRatingRepository, MatchParticipantRepository matchParticipantRepository) {
        this.userRepository = userRepository;
        this.playerRatingRepository = playerRatingRepository;
        this.matchParticipantRepository = matchParticipantRepository;
    }


    //salveaza evaluarile date de userul curent pentru colegii dintr-un meci
    //nu calculeaza nimic
    public void submitRatings(UUID matchId, String raterUsername, List<PlayerRatingDto> ratings) {
        User rater = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(raterUsername)
                .orElseThrow(() -> new NotFoundException("Rater user not found"));

        if (!matchParticipantRepository.existsById_MatchIdAndId_UserId(matchId, rater.getId())) {
            throw new IllegalStateException("You are not part of this match");
        }

        for (PlayerRatingDto dto : ratings) {
            User rated = userRepository.findById(dto.ratedUserId())
                    .orElseThrow(() -> new NotFoundException("Rated user not found"));

            if (!matchParticipantRepository.existsById_MatchIdAndId_UserId(matchId, rated.getId())) {
                throw new IllegalArgumentException("Rated user is not part of this match");
            }

            if (playerRatingRepository.existsByIdMatchIdAndIdRaterUserIdAndIdRatedUserId(
                    matchId, rater.getId(), rated.getId())) {
                throw new IllegalStateException("You already rated this player for this match");
            }

            if (rated.getPosition() == Position.GOALKEEPER) {
                validateGoalkeeperStats(dto);
            } else {
                validateOutfieldStats(dto);
            }

            PlayerRating rating = PlayerRating.builder()
                    .id(new PlayerRatingId(matchId, rater.getId(), rated.getId()))
                    .match(Match.builder().id(matchId).build())
                    .raterUser(rater)
                    .ratedUser(rated)
                    .pace(dto.pace())
                    .shooting(dto.shooting())
                    .passing(dto.passing())
                    .defending(dto.defending())
                    .dribbling(dto.dribbling())
                    .physical(dto.physical())
                    .gkDiving(dto.gkDiving())
                    .gkHandling(dto.gkHandling())
                    .gkKicking(dto.gkKicking())
                    .gkReflexes(dto.gkReflexes())
                    .gkSpeed(dto.gkSpeed())
                    .gkPositioning(dto.gkPositioning())
                    .comment(dto.comment())
                    .build();

            playerRatingRepository.save(rating);
        }
    }


    private void validateGoalkeeperStats(PlayerRatingDto dto) {

        if (dto.gkDiving() == null ||
                dto.gkHandling() == null ||
                dto.gkKicking() == null ||
                dto.gkReflexes() == null ||
                dto.gkSpeed() == null ||
                dto.gkPositioning() == null) {
            throw new IllegalArgumentException("All goalkeeper stats must be provided");
        }

        if (dto.pace() != null ||
                dto.shooting() != null ||
                dto.passing() != null ||
                dto.defending() != null ||
                dto.dribbling() != null ||
                dto.physical() != null) {
            throw new IllegalArgumentException("Outfield stats must be null for goalkeepers");
        }
    }

    private void validateOutfieldStats(PlayerRatingDto dto) {

        if (dto.pace() == null ||
                dto.shooting() == null ||
                dto.passing() == null ||
                dto.defending() == null ||
                dto.dribbling() == null ||
                dto.physical() == null) {
            throw new IllegalArgumentException("All outfield stats must be provided");
        }

        if (dto.gkDiving() != null ||
                dto.gkHandling() != null ||
                dto.gkKicking() != null ||
                dto.gkReflexes() != null ||
                dto.gkSpeed() != null ||
                dto.gkPositioning() != null) {
            throw new IllegalArgumentException("Goalkeeper stats must be null for outfield players");
        }
    }
}
