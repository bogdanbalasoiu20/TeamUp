package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.matchParticipant.JoinResponseDto;

import java.util.UUID;

public class MatchParticipantMapper {
    public static JoinResponseDto toDto(UUID matchId, int participants, int maxPlayers) {
        return new JoinResponseDto(matchId, participants, maxPlayers);
    }
}
