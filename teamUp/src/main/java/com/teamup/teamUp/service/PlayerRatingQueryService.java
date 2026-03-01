package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.dto.rating.player.PlayerToRateDto;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.repository.MatchParticipantRepository;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PlayerRatingQueryService {

    private final UserRepository userRepository;
    private final MatchParticipantRepository matchParticipantRepository;

    @Autowired
    public  PlayerRatingQueryService(UserRepository userRepository, MatchParticipantRepository matchParticipantRepository) {
        this.userRepository = userRepository;
        this.matchParticipantRepository = matchParticipantRepository;
    }

    //intoarce lista cu toti jucatorii dintr-un meci pentru a le da ratinguri
    public List<PlayerToRateDto> getPlayersToRate(UUID matchId, String username) {

        User rater = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!matchParticipantRepository.existsById_MatchIdAndId_UserId(matchId, rater.getId())) {
            throw new IllegalStateException("You are not part of this match");
        }

        return matchParticipantRepository.findAllById_MatchId(matchId).stream()
                .map(mp -> mp.getUser())
                .filter(u -> !u.getId().equals(rater.getId()))
                .map(u -> new PlayerToRateDto(
                        u.getId(),
                        u.getUsername(),
                        u.getPosition()
                ))
                .toList();
    }
}
