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

@Component
public class ChemistryPairEvaluator {

    private final ArchetypeDetector archetypeDetector;

    public ChemistryPairEvaluator(ArchetypeDetector archetypeDetector) {
        this.archetypeDetector = archetypeDetector;
    }

    /**
     * Evaluează compatibilitatea TACTICĂ și INTERACȚIUNEA directă.
     * Nu se ocupă de similitudinea socială (aia e în Cosine Similarity).
     */
    public PairResult evaluate(
            PlayerCardStats statsA, Position posA, PlayerBehaviorStats behaviorA,
            PlayerCardStats statsB, Position posB, PlayerBehaviorStats behaviorB
    ) {
        List<ChemistryReasons> reasons = new ArrayList<>();

        // 1. IMPORTANȚA RELAȚIEI (PROXIMITY)
        // Dacă jucătorii sunt departe (GK vs ST), impactul interacțiunilor scade.
        double proximityWeight = PositionSynergy.get(posA, posB);

        // 2. DETECTIA ROLURILOR
        PlayerArchetype roleA = archetypeDetector.detect(statsA, posA);
        PlayerArchetype roleB = archetypeDetector.detect(statsB, posB);

        // 3. SINERGIA DE ROL (TACTIC)
        double roleBonus = RoleSynergy.get(roleA, roleB);

        if (roleBonus > 0.03) {
            reasons.add(new ChemistryReasons("Tactical fit: " + roleA + " & " + roleB, ReasonType.POSITIVE));
        } else if (roleBonus < -0.02) {
            reasons.add(new ChemistryReasons("Role clash: " + roleA + " & " + roleB, ReasonType.NEGATIVE));
        }

        // 4. REGULI DE INTERACȚIUNE (BEHAVIOR CLASHES)
        double interactionImpact = calculateInteractionImpact(roleA, behaviorA, roleB, behaviorB, reasons);

        // 5. CALCUL FINAL
        // Formula: (Bonus Tactic + Impact Comportamental) * Cât de des se întâlnesc pe teren
        double totalImpact = (roleBonus + interactionImpact) * proximityWeight;

        // Adaugam explicatii de proximitate doar daca e relevant
        if (proximityWeight > 0.9 && totalImpact > 0) {
            reasons.add(new ChemistryReasons("Strong link on the field", ReasonType.POSITIVE));
        }

        return new PairResult(totalImpact, reasons);
    }

    private double calculateInteractionImpact(
            PlayerArchetype roleA, PlayerBehaviorStats bA,
            PlayerArchetype roleB, PlayerBehaviorStats bB,
            List<ChemistryReasons> reasons
    ) {
        double impact = 0.0;

        // --- REGULA 1: "Too Many Cooks" (Doi jucători ofensivi egoiști) ---
        boolean bothSelfish = bA.getSelfishness() > 70 && bB.getSelfishness() > 70;
        boolean offensiveContext = isOffensive(roleA) || isOffensive(roleB);

        if (bothSelfish && offensiveContext) {
            impact -= 0.12;
            reasons.add(new ChemistryReasons("Conflict: Both players demand the ball", ReasonType.NEGATIVE));
        }

        // --- REGULA 2: "Radio Silence" (Lipsă de comunicare) ---
        if (bA.getCommunication() < 40 && bB.getCommunication() < 40) {
            impact -= 0.08;
            reasons.add(new ChemistryReasons("Disorganized: Lack of communication", ReasonType.NEGATIVE));
        }

        // --- REGULA 3: "Aggression Risk" (Doi jucători agresivi) ---
        // Asta e riscant mai ales in aparare
        boolean bothAggressive = bA.getAggressiveness() > 80 && bB.getAggressiveness() > 80;
        if (bothAggressive) {
            impact -= 0.06;
            reasons.add(new ChemistryReasons("High risk of fouls (Both Aggressive)", ReasonType.NEGATIVE));
        }

        // --- REGULA 4: "Mentor Bonus" (Un lider calmează un jucător problematic) ---
        // Dacă A e Lider (Comm > 85) și B e dificil (FairPlay < 40 sau Aggro > 80)
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