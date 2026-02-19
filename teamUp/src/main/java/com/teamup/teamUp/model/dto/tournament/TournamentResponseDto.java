package com.teamup.teamUp.model.dto.tournament;


import com.teamup.teamUp.model.enums.TournamentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TournamentResponseDto(
        UUID id,
        String name,
        UUID venueId,
        String venueName,
        Double venueLatitude,
        Double venueLongitude,
        Integer maxTeams,
        TournamentStatus status,
        LocalDateTime startsAt,
        LocalDateTime endsAt
) {}

