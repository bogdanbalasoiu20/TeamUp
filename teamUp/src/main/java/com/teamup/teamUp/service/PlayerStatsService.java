package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.dto.rating.PlayerRatingDto;
import com.teamup.teamUp.model.dto.rating.PlayerToRateDto;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.model.entity.PlayerRating;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.entity.UserStatsView;
import com.teamup.teamUp.model.id.PlayerRatingId;
import com.teamup.teamUp.repository.MatchParticipantRepository;
import com.teamup.teamUp.repository.PlayerRatingRepository;
import com.teamup.teamUp.repository.UserRepository;
import com.teamup.teamUp.repository.UserStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PlayerStatsService {
    private final UserRepository userRepository;
    private final PlayerRatingRepository playerRatingRepository;
    private final UserStatsRepository userStatsRepository;
    private final MatchParticipantRepository matchParticipantRepository;

    @Autowired
    public PlayerStatsService(UserRepository userRepository, PlayerRatingRepository playerRatingRepository, UserStatsRepository userStatsRepository, MatchParticipantRepository matchParticipantRepository) {
        this.userRepository = userRepository;
        this.playerRatingRepository = playerRatingRepository;
        this.userStatsRepository = userStatsRepository;
        this.matchParticipantRepository = matchParticipantRepository;
    }

    //o sa am o singura pagina cu toti userii unui meci, sub fiecare apare sectiunea de feedback. Nu am o pagina separata pentru fiecare user
    @Transactional
    public void submitRatings(UUID matchId, String raterUsername, List<PlayerRatingDto> ratings){
        User rater = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(raterUsername).orElseThrow(()->new NotFoundException("Rater user not found"));

        for(PlayerRatingDto dto : ratings){
            User rated = userRepository.findById(dto.ratedUserId()).orElseThrow(()->new NotFoundException("Rated user not found"));

            PlayerRating playerRating = PlayerRating.builder()
                    .id(new PlayerRatingId(matchId,rater.getId(),rated.getId()))
                    .match(Match.builder().id(matchId).build())
                    .raterUser(rater)
                    .ratedUser(rated)
                    .pace((short)dto.pace())
                    .shooting((short)dto.shooting())
                    .passing((short)dto.passing())
                    .defending((short)dto.defending())
                    .dribbling((short)dto.dribbling())
                    .physical((short)dto.physical())
                    .comment(dto.comment())
                    .build();

            playerRatingRepository.save(playerRating);
        }
    }

    @Transactional
    public UserStatsView getUserStats(String username) {
        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return userStatsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("No ratings yet for this user"));
    }

    //returneaza pagina cu toti participnatii unui meci pentru a fi afisati in pagina de rating
    @Transactional
    public List<PlayerToRateDto> getPlayersToRate(UUID matchId, String raterUsername){
        User rater = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(raterUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!matchParticipantRepository.existsById_MatchIdAndId_UserId(matchId, rater.getId())) {
            throw new NotFoundException("You are not part of this match");
        }

        return matchParticipantRepository.findAllById_MatchId(matchId).stream()
                .map(mp -> mp.getUser())
                .filter(u -> !u.getId().equals(rater.getId()))
                .map(u -> new PlayerToRateDto(
                        u.getId(),
                        u.getUsername(),
                        u.getPosition() != null ? u.getPosition() : null
                ))
                .toList();
    }

}
