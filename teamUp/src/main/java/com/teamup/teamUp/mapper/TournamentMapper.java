package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.tournament.TournamentResponseDto;
import com.teamup.teamUp.model.entity.Tournament;

public class TournamentMapper {

    public static TournamentResponseDto toDto(Tournament t) {
        return new TournamentResponseDto(
                t.getId(),
                t.getName(),
                t.getVenue().getId(),
                t.getVenue().getName(),
                t.getVenue().getLatitude(),
                t.getVenue().getLongitude(),
                t.getMaxTeams(),
                t.getStatus(),
                t.getStartsAt(),
                t.getEndsAt()
                );
    }
}

