package com.teamup.teamUp.model.dto.tournament;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateTournamentRequestDto {
    @NotBlank
    private String name;

    @NotNull
    private UUID venueId;

    @NotNull
    @Min(2)
    private Integer maxTeams;

    @Size(max = 2000)
    private String description;

    @NotNull
    @Max(11)
    @Min(3)
    private Integer playersPerTeam;

    @NotNull
    private LocalDateTime startsAt;

    @NotNull
    private LocalDateTime endsAt;
}
