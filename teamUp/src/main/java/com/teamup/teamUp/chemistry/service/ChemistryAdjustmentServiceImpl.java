package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.ChemistryPairEvaluator;
import com.teamup.teamUp.chemistry.ReasonType;
import com.teamup.teamUp.chemistry.dto.AdjustmentResult;
import com.teamup.teamUp.chemistry.dto.ChemistryReasons;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.entity.PlayerBehaviorStats;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.enums.Position;
import com.teamup.teamUp.repository.MatchParticipantRepository;
import com.teamup.teamUp.repository.PlayerBehaviorStatsRepository;
import com.teamup.teamUp.repository.PlayerCardStatsRepository;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ChemistryAdjustmentServiceImpl implements ChemistryAdjustmentService {

    private final PlayerCardStatsRepository cardStatsRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final UserRepository userRepository;
    private final PlayerBehaviorStatsRepository playerBehaviorStatsRepository;
    private final ChemistryPairEvaluator pairEvaluator;

    public ChemistryAdjustmentServiceImpl(
            PlayerCardStatsRepository cardStatsRepository,
            MatchParticipantRepository matchParticipantRepository,
            UserRepository userRepository,
            PlayerBehaviorStatsRepository playerBehaviorStatsRepository,
            ChemistryPairEvaluator pairEvaluator
    ) {
        this.cardStatsRepository = cardStatsRepository;
        this.matchParticipantRepository = matchParticipantRepository;
        this.userRepository = userRepository;
        this.playerBehaviorStatsRepository = playerBehaviorStatsRepository;
        this.pairEvaluator = pairEvaluator;
    }

    @Override
    public AdjustmentResult adjust(UUID userA, UUID userB, double similarity) {

        // Nota: 'similarity' care vine aici este acum strict "Behavior Cosine Similarity" (Social Vibe)
        double adjusted = similarity;
        List<ChemistryReasons> reasons = new ArrayList<>();

        // 1. INCARCARE DATE (Fail-fast daca lipsesc date critice)
        PlayerCardStats statsA = cardStatsRepository.findById(userA)
                .orElseThrow(() -> new NotFoundException("Card stats not found for user A"));
        PlayerCardStats statsB = cardStatsRepository.findById(userB)
                .orElseThrow(() -> new NotFoundException("Card stats not found for user B"));

        PlayerBehaviorStats behaviorA = playerBehaviorStatsRepository.findByUser_Id(userA)
                .orElseThrow(() -> new NotFoundException("Behavior stats not found for user A"));
        PlayerBehaviorStats behaviorB = playerBehaviorStatsRepository.findByUser_Id(userB)
                .orElseThrow(() -> new NotFoundException("Behavior stats not found for user B"));

        Position posA = userRepository.findById(userA).orElseThrow().getPosition();
        Position posB = userRepository.findById(userB).orElseThrow().getPosition();

        // 2. FACTORI GLOBALI (Care nu depind de roluri)
        // Level Difference
        Double ratingA = cardStatsRepository.getOverall(userA);
        Double ratingB = cardStatsRepository.getOverall(userB);

        if (ratingA != null && ratingB != null) {
            double diff = Math.abs(ratingA - ratingB);
            if (diff > 15) {
                // Penalizare dinamica: 0.05 pt 15 diff -> max 0.15
                double penalty = Math.min(0.15, (diff - 15) * 0.01);
                adjusted -= penalty;
                reasons.add(new ChemistryReasons("Large skill gap", ReasonType.NEGATIVE));
            }
        }

        // Match History
        int matchesTogether = matchParticipantRepository.countMatchesTogether(userA, userB);
        if (matchesTogether > 0) {
            double bonus = Math.min(0.10, matchesTogether * 0.02); // 5 meciuri = max bonus
            adjusted += bonus;
            reasons.add(new ChemistryReasons("Veterans together", ReasonType.POSITIVE));
        }

        // 3. EVALUAREA TACTICA SI COMPORTAMENTALA (Delegare catre Evaluator)
        // Aici se intampla toata magia cu roluri si interactiuni
        var pairResult = pairEvaluator.evaluate(statsA, posA, behaviorA, statsB, posB, behaviorB);

        adjusted += pairResult.impactScore();
        reasons.addAll(pairResult.reasons());

        // 4. CLAMP FINAL
        adjusted = Math.max(0.0, Math.min(1.0, adjusted));

        return new AdjustmentResult(adjusted, reasons);
    }
}