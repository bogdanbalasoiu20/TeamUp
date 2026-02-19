package com.teamup.teamUp.model.dto.tournament;


import com.teamup.teamUp.model.enums.TournamentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TournamentResponseDto (
        UUID id,
        String name,
        Double latitude,
        Double longitude,
        Integer maxTeams,
        TournamentStatus status,
        LocalDateTime startsAt,
        LocalDateTime endsAt
){}

