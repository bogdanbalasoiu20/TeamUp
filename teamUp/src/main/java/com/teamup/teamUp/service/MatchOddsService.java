package com.teamup.teamUp.service;

import com.teamup.teamUp.chemistry.service.TeamChemistryService;
import com.teamup.teamUp.model.dto.odds.MatchOddsDto;
import com.teamup.teamUp.model.entity.TeamMember;
import com.teamup.teamUp.model.entity.TournamentMatch;
import com.teamup.teamUp.model.enums.SquadType;
import com.teamup.teamUp.repository.TeamMemberRepository;
import com.teamup.teamUp.repository.TournamentMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MatchOddsService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRatingService teamRatingService;
    private final TeamChemistryService teamChemistryService;
    private final LiveFormService liveFormService;
    private final TournamentMatchRepository tournamentMatchRepository;

    public MatchOddsDto calculateMatchOdds(UUID homeTeamId, UUID awayTeamId) {

        double homeScore = calculateTeamScore(homeTeamId, awayTeamId);
        double awayScore = calculateTeamScore(awayTeamId, homeTeamId);

        double max = Math.max(homeScore, awayScore);

        double scaleFactor = 0.08;

        double expHome = Math.exp((homeScore - max) * scaleFactor);
        double expAway = Math.exp((awayScore - max) * scaleFactor);

        double baseTotal = expHome + expAway;
        double pHome = expHome / baseTotal;
        double pAway = expAway / baseTotal;

        double diff = Math.abs(homeScore - awayScore);


        double normalizedDiff = diff / 50.0;

// draw realist
        double baseDraw = 0.22;

// decay mai agresiv
        double pDraw = baseDraw * Math.exp(-3.5 * normalizedDiff);

// clamp
        pDraw = clamp(pDraw, 0.10, 0.26);



        double scale = 1 - pDraw;
        pHome *= scale;
        pAway *= scale;

        double total = pHome + pAway + pDraw;
        pHome /= total;
        pAway /= total;
        pDraw /= total;

        double alpha = 0;

        pHome = pHome * (1 - alpha) + alpha / 3;
        pAway = pAway * (1 - alpha) + alpha / 3;
        pDraw = pDraw * (1 - alpha) + alpha / 3;

        pHome = clamp(pHome, 0.01, 0.98);
        pAway = clamp(pAway, 0.01, 0.98);
        pDraw = clamp(pDraw, 0.01, 0.98);

        double total2 = pHome + pAway + pDraw;
        pHome /= total2;
        pAway /= total2;
        pDraw /= total2;

        double margin = 1.03;

        double homeOdds = margin / pHome;
        double drawOdds = margin / pDraw;
        double awayOdds = margin / pAway;

        homeOdds = clamp(homeOdds, 1.05, 20);
        drawOdds = clamp(drawOdds, 2, 30);
        awayOdds = clamp(awayOdds, 2, 50);

        return new MatchOddsDto(
                round(pHome),
                round(pDraw),
                round(pAway),
                round(homeOdds),
                round(drawOdds),
                round(awayOdds)
        );
    }

    private double calculateTeamScore(UUID teamId, UUID opponentId) {

        double rating = teamRatingService.calculateTeamRating(teamId).overall();
        double chemistry = teamChemistryService.calculateTeamChemistry(teamId).teamChemistry();
        double form = calculateTeamForm(teamId);
        double liveForm = calculateTeamLiveForm(teamId);
        double headToHead = calculateHeadToHead(teamId, opponentId);


        double score = rating * 0.45 +
                        chemistry * 0.30 +
                        (form * 50) * 0.15 +
                        (liveForm * 50) * 0.05 +
                        (headToHead * 50) * 0.05;

        return score;
    }


    private double calculateTeamLiveForm(UUID teamId) {

        List<TeamMember> starters = teamMemberRepository.findByTeamIdAndSquadType(teamId, SquadType.PITCH);

        double avg = starters.stream()
                        .map(s -> liveFormService.calculateLiveForm(s.getUser().getId()))
                        .filter(dto -> dto.matchesCount() > 0)
                        .mapToDouble(dto -> dto.delta())
                        .average()
                        .orElse(0.0);

        return clamp(avg, -1, 1);
    }


    private double calculateTeamForm(UUID teamId) {

        List<TournamentMatch> lastMatches = tournamentMatchRepository.findLastMatches(teamId)
                        .stream()
                        .limit(5)
                        .toList();

        if (lastMatches.isEmpty()) return 0;

        double sum = 0;

        for (TournamentMatch match : lastMatches) {
            sum += getResultForTeam(match, teamId);
        }

        double avg = sum / lastMatches.size();

        return clamp(avg, -1, 1);
    }

    private int getResultForTeam(TournamentMatch match, UUID teamId) {

        if (match.getScoreHome() == null || match.getScoreAway() == null) {
            return 0;
        }

        if (match.getHomeTeam().getId().equals(teamId)) {

            if (match.getScoreHome() > match.getScoreAway()) return 1;
            if (match.getScoreHome() < match.getScoreAway()) return -1;
            return 0;

        } else {

            if (match.getScoreAway() > match.getScoreHome()) return 1;
            if (match.getScoreAway() < match.getScoreHome()) return -1;
            return 0;
        }
    }


    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }


    private double calculateHeadToHead(UUID teamAId, UUID teamBId) {

        List<TournamentMatch> matches = tournamentMatchRepository.findHeadToHeadMatches(teamAId, teamBId)
                        .stream()
                        .limit(5)
                        .toList();

        if (matches.isEmpty()) return 0;

        double sum = 0;

        for (TournamentMatch match : matches) {
            sum += getResultForTeam(match, teamAId);
        }

        double avg = sum / matches.size();

        return clamp(avg, -1, 1);
    }
}