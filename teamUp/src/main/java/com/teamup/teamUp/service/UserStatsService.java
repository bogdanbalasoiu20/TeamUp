package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.dto.user.UserStatsDto;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserStatsService {
    private final MatchRepository matchRepository;
    private final MatchParticipantRepository matchPlayerRepository;
    private final PlayerRatingRepository ratingRepository;
    private final PlayerCardStatsRepository cardRepository;
    private final PlayerCardStatsHistoryRepository cardHistoryRepository;

    public UserStatsService(MatchRepository matchRepository, MatchParticipantRepository matchPlayerRepository, PlayerRatingRepository ratingRepository, PlayerCardStatsRepository cardRepository,  PlayerCardStatsHistoryRepository cardHistoryRepository) {
        this.matchRepository = matchRepository;
        this.matchPlayerRepository = matchPlayerRepository;
        this.ratingRepository = ratingRepository;
        this.cardRepository = cardRepository;
        this.cardHistoryRepository = cardHistoryRepository;
    }

    public UserStatsDto getUserStats(UUID userId) {
        int matchesPlayed = matchPlayerRepository.countByUser_Id(userId);
        int matchesCreated = matchRepository.countByCreator_Id(userId);

        int votesGiven = ratingRepository.countByRaterUser_Id(userId);
        int votesReceived = ratingRepository.countByRatedUser_Id(userId);

        PlayerCardStats card = cardRepository.findById(userId).orElseThrow(() -> new NotFoundException("Player card not found"));
        double maxRating = Math.max(card.getOverallRating(), cardHistoryRepository.findMaxOverallByUserId(userId));



        return new UserStatsDto(
                matchesPlayed,
                matchesCreated,
                votesGiven,
                votesReceived,
                card.getOverallRating(),
                maxRating
        );
    }
}
