package com.teamup.teamUp.service;

import com.teamup.teamUp.chemistry.TeamChemistryManager;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.dto.odds.MatchOddsDto;
import com.teamup.teamUp.model.entity.*;
import com.teamup.teamUp.model.enums.MatchStatus;
import com.teamup.teamUp.model.enums.SquadType;
import com.teamup.teamUp.model.id.TournamentMatchParticipantId;
import com.teamup.teamUp.repository.TeamMemberRepository;
import com.teamup.teamUp.repository.TournamentMatchParticipantRepository;
import com.teamup.teamUp.repository.TournamentMatchRepository;
import com.teamup.teamUp.repository.TournamentStandingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TournamentMatchService {

    private final TournamentMatchRepository matchRepository;
    private final TournamentStandingRepository standingRepository;
    private final TournamentService tournamentService;
    private final TeamMemberRepository teamMemberRepository;
    private final TournamentMatchParticipantRepository participantRepository;
    private final TeamChemistryManager teamChemistryManager;
    private final MatchOddsService matchOddsService;
    private final RatingUpdateService ratingUpdateService;

    @Transactional
    public void finishMatch(UUID matchId, int scoreHome, int scoreAway) {

        TournamentMatch match = matchRepository.findById(matchId).orElseThrow(() -> new NotFoundException("Match not found"));

        if (match.getStatus() == MatchStatus.DONE) {
            throw new RuntimeException("Match already finished");
        }

        if (scoreHome < 0 || scoreAway < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();

        MatchOddsDto odds = matchOddsService.calculateMatchOdds(home.getId(), away.getId());

        match.setOddsHome(odds.homeWinOdds());
        match.setOddsDraw(odds.drawOdds());
        match.setOddsAway(odds.awayWinOdds());

        match.setScoreHome(scoreHome);
        match.setScoreAway(scoreAway);
        match.setStatus(MatchStatus.DONE);
        snapshotLineup(match);


        if (scoreHome > scoreAway) {
            match.setWinner(home);
        } else if (scoreAway > scoreHome) {
            match.setWinner(away);
        } else {
            match.setWinner(null); // draw
        }


        TournamentStanding homeStanding = standingRepository.findByTournamentIdAndTeamId(match.getTournament().getId(), home.getId()).orElseThrow();

        TournamentStanding awayStanding = standingRepository.findByTournamentIdAndTeamId(match.getTournament().getId(), away.getId()).orElseThrow();

        updateStandings(homeStanding, awayStanding, scoreHome, scoreAway);

        matchRepository.save(match);

        List<TournamentMatchParticipant> participants = participantRepository.findByMatchId(match.getId());

        ratingUpdateService.updateAfterTournamentMatch(match, participants);

        tournamentService.finishTournamentIfCompleted(match.getTournament().getId());

        teamChemistryManager.recalcTeamChemistry(home.getId());
        teamChemistryManager.recalcTeamChemistry(away.getId());
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


    private void snapshotLineup(TournamentMatch match) {

        if (participantRepository.existsByMatch_Id(match.getId())) {
            return;
        }

        saveParticipantsForTeam(match, match.getHomeTeam());
        saveParticipantsForTeam(match, match.getAwayTeam());
    }

    private void saveParticipantsForTeam(TournamentMatch match, Team team) {
        List<TeamMember> lineup = teamMemberRepository.findByTeamIdAndSquadType(team.getId(), SquadType.PITCH);

        if (lineup.isEmpty()) {
            return;
        }

        List<TournamentMatchParticipant> participants = lineup.stream()
                .map(member -> TournamentMatchParticipant.builder()
                                .id(new TournamentMatchParticipantId(
                                        match.getId(),
                                        member.getUser().getId()
                                ))
                                .match(match)
                                .user(member.getUser())
                                .team(team)
                                .build()
                ).toList();

        participantRepository.saveAll(participants);
    }
}

