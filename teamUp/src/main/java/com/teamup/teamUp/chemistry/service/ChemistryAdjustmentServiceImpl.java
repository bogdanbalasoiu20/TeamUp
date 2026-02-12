package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.ChemistryPairEvaluator;
import com.teamup.teamUp.chemistry.ReasonType;
import com.teamup.teamUp.chemistry.dto.AdjustmentResult;
import com.teamup.teamUp.chemistry.dto.ChemistryReasons;
import com.teamup.teamUp.model.entity.PlayerBehaviorStats;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.enums.Position;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChemistryAdjustmentServiceImpl implements ChemistryAdjustmentService {

    private final ChemistryPairEvaluator pairEvaluator;

    public ChemistryAdjustmentServiceImpl(ChemistryPairEvaluator pairEvaluator) {
        this.pairEvaluator = pairEvaluator;
    }

    @Override
    public AdjustmentResult adjust(
            double similarity,
            PlayerCardStats statsA, Position posA, PlayerBehaviorStats behaviorA,
            PlayerCardStats statsB, Position posB, PlayerBehaviorStats behaviorB,
            int matchesTogether, int matchesPlayedUserA, int matchesPlayedUserB
    ) {

        //MATURITY FACTOR
        double maturityA = Math.min(1.0, matchesPlayedUserA / 10.0);
        double maturityB = Math.min(1.0, matchesPlayedUserB / 10.0);

        // media maturitatii
        double maturityFactor = (maturityA + maturityB) / 2.0;

        // behavior porneste de la 0.15 și urca pana la 0.4
        double behaviorWeight = 0.15 + (0.25 * maturityFactor);

        // pastram tactica constanta
        double tacticalWeight = 0.4;

        // experienta constanta
        double experienceWeight = 0.2;

        // normalizare ca suma sa fie 1
        double totalWeight = behaviorWeight + tacticalWeight + experienceWeight;

        behaviorWeight /= totalWeight;
        tacticalWeight /= totalWeight;
        experienceWeight /= totalWeight;

        List<ChemistryReasons> reasons = new ArrayList<>();

        var pairResult = pairEvaluator.evaluate(statsA, posA, behaviorA, statsB, posB, behaviorB, matchesPlayedUserA, matchesPlayedUserB);

        double tacticalScore = 0.5 + pairResult.impactScore();
        double experienceScore = Math.min(1.0, matchesTogether*0.1);

        if (matchesTogether == 0) {
            reasons.add(new ChemistryReasons("No shared match history yet", ReasonType.NEUTRAL));
        }else if (matchesTogether == 1) {
            reasons.add(new ChemistryReasons("1 match together", ReasonType.POSITIVE));
        } else {
            reasons.add(new ChemistryReasons(matchesTogether + " matches together", ReasonType.POSITIVE));
        }



        double adjusted = behaviorWeight * similarity +
                        tacticalWeight * tacticalScore +
                        experienceWeight * experienceScore;

        double ratingA = statsA.getOverallRating();
        double ratingB = statsB.getOverallRating();

        double diff = Math.abs(ratingA - ratingB);
        if (diff > 4) {
            double penalty = Math.min(0.30, (diff - 4) * 0.04);
            adjusted -= penalty;
            reasons.add(new ChemistryReasons("Large skill gap", ReasonType.NEGATIVE));
        }

        int expDiff = Math.abs(matchesPlayedUserA - matchesPlayedUserB);
        int maxMatches = Math.max(matchesPlayedUserA, matchesPlayedUserB);
        if (expDiff > 5 && maxMatches > 0) {
            double ratio = (double) expDiff / maxMatches;
            double penalty = Math.min(0.12, ratio * 0.12);
            adjusted -= penalty;
            reasons.add(new ChemistryReasons("Experience imbalance", ReasonType.NEGATIVE));
        }



        reasons.addAll(pairResult.reasons());

        adjusted = Math.max(0.0, Math.min(1.0, adjusted));

        return new AdjustmentResult(adjusted, reasons, pairResult.roleA(), pairResult.roleB());
    }
}