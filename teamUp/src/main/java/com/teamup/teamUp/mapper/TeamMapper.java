package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.team.TeamResponseDto;
import com.teamup.teamUp.model.entity.Team;

public class TeamMapper {

    public static TeamResponseDto toDto(Team team) {
        return new TeamResponseDto(
                team.getId(),
                team.getName(),
                team.getCaptain().getId(),
                team.getCaptain().getUsername(),
                team.getTeamRating(),
                team.getTeamChemistry(),
                team.getWins(),
                team.getDraws(),
                team.getLosses(),
                team.getMembers() != null ? team.getMembers().size() : 0,
                team.getCreatedAt()
        );
    }
}

