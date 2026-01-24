package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.card.PlayerCardHistoryPointDto;
import com.teamup.teamUp.model.entity.PlayerCardStatsHistory;
import org.springframework.stereotype.Component;

@Component
public class PlayerCardHistoryMapper {
    public static PlayerCardHistoryPointDto toDto(PlayerCardStatsHistory h) {
        return PlayerCardHistoryPointDto.builder()
                .matchId(h.getMatchId())
                .recordedAt(h.getRecordedAt())
                .overallRating(h.getOverallRating())

                .pace(h.getPace())
                .shooting(h.getShooting())
                .passing(h.getPassing())
                .dribbling(h.getDribbling())
                .defending(h.getDefending())
                .physical(h.getPhysical())

                .gkDiving(h.getGkDiving())
                .gkHandling(h.getGkHandling())
                .gkKicking(h.getGkKicking())
                .gkReflexes(h.getGkReflexes())
                .gkSpeed(h.getGkSpeed())
                .gkPositioning(h.getGkPositioning())
                .build();
    }
}

