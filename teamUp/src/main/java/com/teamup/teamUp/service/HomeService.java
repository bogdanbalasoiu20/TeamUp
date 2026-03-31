package com.teamup.teamUp.service;

import com.teamup.teamUp.model.dto.dashboard.HomeUpcomingResponse;
import com.teamup.teamUp.model.dto.dashboard.UpcomingMatchDto;
import com.teamup.teamUp.model.dto.dashboard.UpcomingTournamentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final MatchService matchService;
    private final TournamentService tournamentService;

    public HomeUpcomingResponse getUpcomingForCurrentUser(String username) {

        List<UpcomingMatchDto> matches = matchService.getUpcomingMatchesForUser(username);

        List<UpcomingTournamentDto> tournaments = tournamentService.getUpcomingTournamentsForUser(username);

        return new HomeUpcomingResponse(matches, tournaments);
    }
}
