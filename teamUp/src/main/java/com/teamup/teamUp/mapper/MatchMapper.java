package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.match.MatchResponseDto;
import com.teamup.teamUp.model.entity.Match;
import org.springframework.stereotype.Component;

@Component
public class MatchMapper {
    public MatchResponseDto toDto(Match match){
        return new MatchResponseDto(
                match.getId(),
                match.getCreator(),
                match.getVenue(),
                match.getStartsAt(),
                match.getEndsAt(),
                match.getDurationMinutes(),
                match.getMaxPlayers(),
                match.getCurrentPlayers(),
                match.getJoinDeadline(),
                match.getTitle(),
                match.getNotes(),
                match.getStatus(),
                match.getVisibility(),
                match.getTotalPrice(),
                match.getCreatedAt(),
                match.getUpdatedAt()
        );
    }
}
