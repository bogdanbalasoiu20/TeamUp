package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.BadRequestException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.dto.rating.PlayerRatingDto;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.model.entity.PlayerBehaviorRating;
import com.teamup.teamUp.model.entity.PlayerRating;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.Position;
import com.teamup.teamUp.model.id.PlayerBehaviorRatingId;
import com.teamup.teamUp.model.id.PlayerRatingId;
import com.teamup.teamUp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.HOURS;


@Service
@Transactional
public class PlayerRatingService {

    private final UserRepository userRepository;
    private final PlayerRatingRepository playerRatingRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final MatchRepository matchRepository;
    private final PlayerBehaviorRatingRepository playerBehaviorRatingRepository;


    public PlayerRatingService(UserRepository userRepository, PlayerRatingRepository playerRatingRepository, MatchParticipantRepository matchParticipantRepository, MatchRepository matchRepository, PlayerBehaviorRatingRepository playerBehaviorRatingRepository) {
        this.userRepository = userRepository;
        this.playerRatingRepository = playerRatingRepository;
        this.matchParticipantRepository = matchParticipantRepository;
        this.matchRepository = matchRepository;
        this.playerBehaviorRatingRepository = playerBehaviorRatingRepository;
    }


    //salveaza evaluarile date de userul curent pentru colegii dintr-un meci
    //nu calculeaza nimic
    public void submitRatings(UUID matchId, String raterUsername, List<PlayerRatingDto> ratings) {

        User rater = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(raterUsername).orElseThrow(() -> new NotFoundException("Rater user not found"));

        Match match = matchRepository.findById(matchId).orElseThrow(() -> new NotFoundException("Match not found"));

        if (!matchParticipantRepository.existsById_MatchIdAndId_UserId(matchId, rater.getId())) {
            throw new IllegalStateException("You are not part of this match");
        }

        if (Instant.now().isAfter(match.getRatingOpenedAt().plus(24, HOURS))) {
            throw new BadRequestException("Rating period has ended");
        }

        for (PlayerRatingDto dto : ratings) {

            User rated = userRepository.findById(dto.ratedUserId()).orElseThrow(() -> new NotFoundException("Rated user not found"));

            if (!matchParticipantRepository.existsById_MatchIdAndId_UserId(matchId, rated.getId())) {
                throw new IllegalArgumentException("Rated user is not part of this match");
            }

            boolean hasBehavior = dto.fairPlay() != null ||
                            dto.communication() != null ||
                            dto.fun() != null ||
                            dto.competitiveness() != null ||
                            dto.selfishness() != null ||
                            dto.aggressiveness() != null;

            if (playerRatingRepository.existsByIdMatchIdAndIdRaterUserIdAndIdRatedUserId(matchId, rater.getId(), rated.getId())) {
                throw new IllegalStateException("You already rated this player for this match");
            }

            if (hasBehavior && playerBehaviorRatingRepository.existsByIdMatchIdAndIdRaterUserIdAndIdRatedUserId(matchId, rater.getId(), rated.getId())) {
                throw new IllegalStateException("You already rated this player's behavior for this match");
            }

            if (rated.getPosition() == Position.GOALKEEPER) {
                validateGoalkeeperStats(dto);
            } else {
                validateOutfieldStats(dto);
            }

            if (hasBehavior) {
                validateBehaviorStats(dto);
            }

            PlayerRating rating = PlayerRating.builder()
                    .id(new PlayerRatingId(matchId, rater.getId(), rated.getId()))
                    .match(match)
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
                    .build();

            playerRatingRepository.save(rating);

            if (hasBehavior) {
                PlayerBehaviorRating behaviorRating = PlayerBehaviorRating.builder()
                        .id(new PlayerBehaviorRatingId(matchId, rater.getId(), rated.getId()))
                        .match(match)
                        .raterUser(rater)
                        .ratedUser(rated)
                        .fairPlay(dto.fairPlay())
                        .communication(dto.communication())
                        .fun(dto.fun())
                        .competitiveness(dto.competitiveness())
                        .selfishness(dto.selfishness())
                        .aggressiveness(dto.aggressiveness())
                        .build();

                playerBehaviorRatingRepository.save(behaviorRating);
            }
        }
    }



    private void validateGoalkeeperStats (PlayerRatingDto dto){

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

        private void validateOutfieldStats (PlayerRatingDto dto){

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

        private void validateBehaviorStats (PlayerRatingDto dto){
            if (dto.fairPlay() == null &&
                    dto.communication() == null &&
                    dto.fun() == null &&
                    dto.competitiveness() == null &&
                    dto.selfishness() == null &&
                    dto.aggressiveness() == null) {
                return;
            }

            if (dto.fairPlay() == null ||
                    dto.communication() == null ||
                    dto.fun() == null ||
                    dto.competitiveness() == null ||
                    dto.selfishness() == null ||
                    dto.aggressiveness() == null) {
                throw new IllegalArgumentException("All behavior stats must be provided");
            }
        }

}

