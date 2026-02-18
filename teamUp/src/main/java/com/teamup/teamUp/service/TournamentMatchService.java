package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.entity.Team;
import com.teamup.teamUp.model.entity.TournamentMatch;
import com.teamup.teamUp.model.entity.TournamentStanding;
import com.teamup.teamUp.model.enums.MatchStatus;
import com.teamup.teamUp.repository.TournamentMatchRepository;
import com.teamup.teamUp.repository.TournamentStandingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TournamentMatchService {

    private final TournamentMatchRepository matchRepository;
    private final TournamentStandingRepository standingRepository;
    private final TournamentService tournamentService;

    @Transactional
    public void finishMatch(UUID matchId, int scoreHome, int scoreAway) {

        TournamentMatch match = matchRepository.findById(matchId).orElseThrow(() -> new NotFoundException("Match not found"));

        if (match.getStatus() == MatchStatus.DONE) {
            throw new RuntimeException("Match already finished");
        }

        match.setScoreHome(scoreHome);
        match.setScoreAway(scoreAway);
        match.setStatus(MatchStatus.DONE);

        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();

        TournamentStanding homeStanding = standingRepository.findByTournamentIdAndTeamId(match.getTournament().getId(), home.getId()).orElseThrow();

        TournamentStanding awayStanding = standingRepository.findByTournamentIdAndTeamId(match.getTournament().getId(), away.getId()).orElseThrow();

        updateStandings(homeStanding, awayStanding, scoreHome, scoreAway);

        matchRepository.save(match);

        tournamentService.finishTournamentIfCompleted(match.getTournament().getId());
    }

    private void updateStandings(TournamentStanding home, TournamentStanding away, int scoreHome, int scoreAway) {

        home.setPlayed(home.getPlayed() + 1);
        away.setPlayed(away.getPlayed() + 1);

        home.setGoalsFor(home.getGoalsFor() + scoreHome);
        home.setGoalsAgainst(home.getGoalsAgainst() + scoreAway);

        away.setGoalsFor(away.getGoalsFor() + scoreAway);
        away.setGoalsAgainst(away.getGoalsAgainst() + scoreHome);

        if (scoreHome > scoreAway) {
            home.setWins(home.getWins() + 1);
            home.setPoints(home.getPoints() + 3);
            away.setLosses(away.getLosses() + 1);
        } else if (scoreHome < scoreAway) {
            away.setWins(away.getWins() + 1);
            away.setPoints(away.getPoints() + 3);
            home.setLosses(home.getLosses() + 1);
        } else {
            home.setDraws(home.getDraws() + 1);
            away.setDraws(away.getDraws() + 1);
            home.setPoints(home.getPoints() + 1);
            away.setPoints(away.getPoints() + 1);
        }
    }
}

