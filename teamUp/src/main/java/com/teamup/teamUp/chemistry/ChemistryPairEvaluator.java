package com.teamup.teamUp.chemistry;

import com.teamup.teamUp.chemistry.dto.ChemistryReasons;
import com.teamup.teamUp.model.entity.PlayerBehaviorStats;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.enums.PlayerArchetype;
import com.teamup.teamUp.model.enums.Position;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//aici se face principala analiza a compatibilitatii
//clasa se uita la cat de bine se completeaza jucatorii dpdv tactic, ci nu social( pentru social folosesc Cosine)
@Component
public class ChemistryPairEvaluator {

    private final ArchetypeDetector archetypeDetector;

    public ChemistryPairEvaluator(ArchetypeDetector archetypeDetector) {
        this.archetypeDetector = archetypeDetector;
    }

    /**
     * Evalueaza compatibilitatea tactica si interactiunea directa.
     * Nu se ocupa de similitudinea sociala (aia e în Cosine Similarity).
     */
    public PairResult evaluate(
            PlayerCardStats statsA, Position posA, PlayerBehaviorStats behaviorA,
            PlayerCardStats statsB, Position posB, PlayerBehaviorStats behaviorB
    ) {
        List<ChemistryReasons> reasons = new ArrayList<>();

        // cat de des se intalnesc pe teren userii
        double proximityWeight = PositionSynergy.get(posA, posB);

        //stats-urile se transforma in roluri
        PlayerArchetype roleA = archetypeDetector.detect(statsA, posA);
        PlayerArchetype roleB = archetypeDetector.detect(statsB, posB);

        //verific matricea de sinergie
        double roleBonus = RoleSynergy.get(roleA, roleB);

        //explic bonusul/penalizarea pentru combinatia de roluri
        if (roleBonus > 0.03) {
            reasons.add(new ChemistryReasons("Tactical fit: " + roleA + " & " + roleB, ReasonType.POSITIVE));
        } else if (roleBonus < -0.02) {
            reasons.add(new ChemistryReasons("Role clash: " + roleA + " & " + roleB, ReasonType.NEGATIVE));
        }

        // reguli de interactiune separate de Cosine pe behavior
        double interactionImpact = calculateInteractionImpact(roleA, behaviorA, roleB, behaviorB, reasons);

        // calculul final
        double totalImpact = (roleBonus + interactionImpact) * proximityWeight;

        if (proximityWeight > 0.9 && totalImpact > 0) {
            reasons.add(new ChemistryReasons("Strong link on the field", ReasonType.POSITIVE));
        }

        return new PairResult(totalImpact, reasons);
    }


    //reguli hardcodate pentru a evita combinatii neportivite de comportament
    private double calculateInteractionImpact(
            PlayerArchetype roleA, PlayerBehaviorStats bA,
            PlayerArchetype roleB, PlayerBehaviorStats bB,
            List<ChemistryReasons> reasons
    ) {
        double impact = 0.0;

        //penalizare pentru 2 jucatori ofensivi egoisti
        boolean bothSelfish = bA.getSelfishness() > 70 && bB.getSelfishness() > 70;
        boolean offensiveContext = isOffensive(roleA) || isOffensive(roleB);

        if (bothSelfish && offensiveContext) {
            impact -= 0.12;
            reasons.add(new ChemistryReasons("Conflict: Both players demand the ball", ReasonType.NEGATIVE));
        }

        //penalizare pentru lipsa de comunicare
        if (bA.getCommunication() < 40 && bB.getCommunication() < 40) {
            impact -= 0.08;
            reasons.add(new ChemistryReasons("Disorganized: Lack of communication", ReasonType.NEGATIVE));
        }

        //doi jucatori agresivi
        boolean bothAggressive = bA.getAggressiveness() > 80 && bB.getAggressiveness() > 80;
        if (bothAggressive) {
            impact -= 0.06;
            reasons.add(new ChemistryReasons("High risk of fouls (Both Aggressive)", ReasonType.NEGATIVE));
        }

        //ofer un plus pentru un "lider" in combinatie cu un egoist
        boolean hasLeader = bA.getCommunication() > 85 || bB.getCommunication() > 85;
        boolean hasTroubleMaker = bA.getFairPlay() < 40 || bB.getFairPlay() < 40;

        if (hasLeader && hasTroubleMaker) {
            impact += 0.05;
            reasons.add(new ChemistryReasons("Captain influence balances behavior", ReasonType.POSITIVE));
        }

        return impact;
    }

    private boolean isOffensive(PlayerArchetype role) {
        return Set.of(
                PlayerArchetype.POACHER,
                PlayerArchetype.SPEEDSTER,
                PlayerArchetype.TARGET_MAN,
                PlayerArchetype.PLAYMAKER
        ).contains(role);
    }

    public record PairResult(double impactScore, List<ChemistryReasons> reasons) {}
}