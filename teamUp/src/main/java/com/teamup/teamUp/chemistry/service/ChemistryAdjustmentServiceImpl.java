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

        double behaviorWeight = 0.4;
        double tacticalWeight = 0.4;
        double experienceWeight = 0.2;

        var pairResult = pairEvaluator.evaluate(statsA, posA, behaviorA, statsB, posB, behaviorB, matchesPlayedUserA, matchesPlayedUserB);

        double tacticalScore = 0.5 + pairResult.impactScore();
        double experienceScore = Math.min(1.0, matchesTogether*0.1);
        double adjusted = behaviorWeight * similarity +
                        tacticalWeight * tacticalScore +
                        experienceWeight * experienceScore;

        List<ChemistryReasons> reasons = new ArrayList<>();

        double ratingA = statsA.getOverallRating();
        double ratingB = statsB.getOverallRating();

        double diff = Math.abs(ratingA - ratingB);
        if (diff > 2) {
            double penalty = Math.min(0.30, (diff - 2) * 0.05);
            adjusted -= penalty;
            reasons.add(new ChemistryReasons("Large skill gap", ReasonType.NEGATIVE));
        }

        int expDiff = Math.abs(matchesPlayedUserA - matchesPlayedUserB);
        if(expDiff > 5) {
            double  penalty = Math.min(0.20, (expDiff - 5) * 0.02);
            adjusted -= penalty;
            reasons.add(new ChemistryReasons("Experience imbalance", ReasonType.NEGATIVE));
        }


        reasons.addAll(pairResult.reasons());

        adjusted = Math.max(0.0, Math.min(1.0, adjusted));

        return new AdjustmentResult(adjusted, reasons, pairResult.roleA(), pairResult.roleB());
    }
}