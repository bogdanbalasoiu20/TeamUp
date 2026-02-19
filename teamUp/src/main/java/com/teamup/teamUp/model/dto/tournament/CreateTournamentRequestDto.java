package com.teamup.teamUp.model.dto.tournament;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTournamentRequestDto {
    private String name;
    private Double latitude;
    private Double longitude;
    private Integer maxTeams;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
}
