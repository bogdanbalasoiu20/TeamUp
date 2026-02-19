package com.teamup.teamUp.model.dto.tournament;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateTournamentRequestDto {
    private String name;
    private UUID venueId;
    private Integer maxTeams;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
}
