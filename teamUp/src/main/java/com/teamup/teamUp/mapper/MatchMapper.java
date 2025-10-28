package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.match.MatchResponseDto;
import com.teamup.teamUp.model.dto.user.UserSummaryDto;
import com.teamup.teamUp.model.dto.venue.VenueSummaryDto;
import com.teamup.teamUp.model.entity.Match;
import org.springframework.stereotype.Component;

@Component
public class MatchMapper {
    public MatchResponseDto toDto(Match match){
        var creator = new UserSummaryDto(
               match.getCreator().getId(),
               match.getCreator().getUsername(),
               match.getCreator().getPosition(),
               match.getCreator().getRank()
        );

        var venue = new VenueSummaryDto(
                match.getVenue().getId(),
                match.getVenue().getName(),
                match.getVenue().getCity().getSlug(),
                match.getVenue().getLatitude(),
                match.getVenue().getLongitude()
        );

        return new MatchResponseDto(
                match.getId(),
                match.getCreator().getId(),
                match.getVenue().getId(),
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
                match.getUpdatedAt(),
                creator,
                venue
        );
    }
}
