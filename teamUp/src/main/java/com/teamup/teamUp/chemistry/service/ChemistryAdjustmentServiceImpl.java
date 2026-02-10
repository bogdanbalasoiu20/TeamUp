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
            int matchesTogether
    ) {

        double adjusted = similarity;
        List<ChemistryReasons> reasons = new ArrayList<>();

        double ratingA = statsA.getOverallRating();
        double ratingB = statsB.getOverallRating();

        double diff = Math.abs(ratingA - ratingB);
        if (diff > 15) {
            double penalty = Math.min(0.15, (diff - 15) * 0.01);
            adjusted -= penalty;
            reasons.add(new ChemistryReasons("Large skill gap", ReasonType.NEGATIVE));
        }

        if (matchesTogether > 0) {
            double bonus = Math.min(0.10, matchesTogether * 0.02);
            adjusted += bonus;
            reasons.add(new ChemistryReasons("Veterans together", ReasonType.POSITIVE));
        }

        var pairResult = pairEvaluator.evaluate(statsA, posA, behaviorA, statsB, posB, behaviorB);

        adjusted += pairResult.impactScore();
        reasons.addAll(pairResult.reasons());

        adjusted = Math.max(0.0, Math.min(1.0, adjusted));

        return new AdjustmentResult(adjusted, reasons);
    }
}