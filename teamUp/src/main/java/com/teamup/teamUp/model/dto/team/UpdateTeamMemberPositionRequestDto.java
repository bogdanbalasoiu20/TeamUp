package com.teamup.teamUp.model.dto.team;

import com.teamup.teamUp.model.enums.SquadType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateTeamMemberPositionRequestDto(
        @NotNull
        SquadType squadType,

        @NotNull
        @Min(0)
        Integer slotIndex
) {
}
