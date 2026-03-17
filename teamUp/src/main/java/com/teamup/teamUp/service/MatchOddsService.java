package com.teamup.teamUp.service;

import com.teamup.teamUp.chemistry.service.TeamChemistryService;
import com.teamup.teamUp.model.dto.odds.MatchOddsDto;
import com.teamup.teamUp.model.entity.TeamMember;
import com.teamup.teamUp.model.enums.SquadType;
import com.teamup.teamUp.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchOddsService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRatingService teamRatingService;
    private final TeamChemistryService teamChemistryService;
    private final LiveFormService liveFormService;

    public MatchOddsDto calculateMatchOdds(UUID homeTeamId, UUID awayTeamId) {

        double homeScore = calculateTeamScore(homeTeamId);
        double awayScore = calculateTeamScore(awayTeamId);

        // probabilitati
        double pHome = Math.exp(homeScore) / (Math.exp(homeScore) + Math.exp(awayScore));
        double pAway = Math.exp(awayScore) / (Math.exp(homeScore) + Math.exp(awayScore));

        // draw
        double diff = Math.abs(homeScore - awayScore);
        double pDraw = Math.max(0.15, 0.3 - diff);

        // normalizare
        double total = pHome + pAway + pDraw;
        pHome /= total;
        pAway /= total;
        pDraw /= total;

        // odds (cu margina)
        double margin = 1.05;

        double homeOdds = margin / pHome;
        double drawOdds = margin / pDraw;
        double awayOdds = margin / pAway;

        return new MatchOddsDto(
                round(pHome),
                round(pDraw),
                round(pAway),
                round(homeOdds),
                round(drawOdds),
                round(awayOdds)
        );
    }

    private double calculateTeamScore(UUID teamId) {

        double rating = teamRatingService.calculateTeamRating(teamId).overall();
        double chemistry = teamChemistryService.calculateTeamChemistry(teamId).teamChemistry();
        double form = calculateTeamForm(teamId); // [-1,1]
        double liveForm = calculateTeamLiveForm(teamId); // [-1,1]

        // normalizare
        double normRating = rating / 100.0;
        double normChemistry = chemistry / 100.0;

        return 0.5 * normRating + 0.2 * normChemistry + 0.2 * form + 0.1 * liveForm;
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
        // TODO: de implementat forma echipei in ultimele 3-5 meciuri
        return 0;
    }


    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}