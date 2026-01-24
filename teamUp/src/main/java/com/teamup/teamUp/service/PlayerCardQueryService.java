package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.mapper.PlayerCardHistoryMapper;
import com.teamup.teamUp.mapper.PlayerCardMapper;
import com.teamup.teamUp.model.dto.card.PlayerCardDto;
import com.teamup.teamUp.model.dto.card.PlayerCardHistoryPointDto;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.entity.PlayerCardStatsHistory;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.repository.PlayerCardStatsHistoryRepository;
import com.teamup.teamUp.repository.PlayerCardStatsRepository;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PlayerCardQueryService {

    private final PlayerCardStatsRepository cardStatsRepository;
    private final PlayerCardStatsHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final PlayerCardMapper  cardMapper;

    public PlayerCardQueryService(PlayerCardStatsRepository cardStatsRepository, PlayerCardStatsHistoryRepository historyRepository, UserRepository userRepository, PlayerCardMapper  cardMapper) {
        this.cardStatsRepository = cardStatsRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.cardMapper = cardMapper;
    }

    //cardul live al userului
    public PlayerCardDto getLiveCard(UUID userId) {
        PlayerCardStats stats = cardStatsRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Player card not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return cardMapper.mapToDto(stats, user);
    }



    //istoric card pt grafice
    public List<PlayerCardHistoryPointDto> getCardHistory(UUID userId) {
        return historyRepository.findByUserIdOrderByRecordedAtAsc(userId)
                .stream()
                .map(PlayerCardHistoryMapper::toDto)
                .toList();
    }

}
