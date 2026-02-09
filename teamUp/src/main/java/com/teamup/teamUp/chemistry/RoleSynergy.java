package com.teamup.teamUp.chemistry;

import com.teamup.teamUp.model.enums.PlayerArchetype;
import java.util.EnumMap;
import java.util.Map;

import static com.teamup.teamUp.model.enums.PlayerArchetype.*;

public final class RoleSynergy {

    // Folosim EnumMap pentru ca avem >10 intrari si e cel mai rapid
    private static final Map<PlayerArchetype, Map<PlayerArchetype, Double>> SYNERGY = new EnumMap<>(PlayerArchetype.class);

    static {
        // ================= MIDFIELD =================
        add(PLAYMAKER, DESTROYER,  +0.08);
        add(PLAYMAKER, BOX_TO_BOX, +0.05);
        add(PLAYMAKER, SPEEDSTER,  +0.04);
        add(PLAYMAKER, POACHER,    +0.03); // Assist -> Gol
        add(PLAYMAKER, PLAYMAKER,  -0.05);
        add(PLAYMAKER, TARGET_MAN, +0.03);

        add(DESTROYER, PLAYMAKER,  +0.08);
        add(DESTROYER, BOX_TO_BOX, +0.04);
        add(DESTROYER, DESTROYER,  -0.06);

        add(BOX_TO_BOX, PLAYMAKER, +0.05);
        add(BOX_TO_BOX, DESTROYER, +0.04);
        add(BOX_TO_BOX, BOX_TO_BOX,+0.02);

        // ================= ATTACK =================
        add(SPEEDSTER, TARGET_MAN, +0.07); // Big Man - Little Man combo
        add(SPEEDSTER, POACHER,    +0.04);
        add(SPEEDSTER, PLAYMAKER,  +0.04);
        add(SPEEDSTER, SPEEDSTER,  -0.04);

        add(POACHER, PLAYMAKER,    +0.03);
        add(POACHER, SPEEDSTER,    +0.04);
        add(POACHER, POACHER,      -0.03);

        add(TARGET_MAN, SPEEDSTER, +0.07);
        add(TARGET_MAN, PLAYMAKER, +0.03);
        add(TARGET_MAN, WING_BACK, +0.10); // <--- CRITIC: Asta lipsea! (Centrari)
        add(TARGET_MAN, TARGET_MAN,-0.05);

        // ================= DEFENSE =================
        add(STOPPER, BALL_PLAYING_CB, +0.06); // Cuplul ideal CB
        add(STOPPER, WING_BACK,       +0.03);
        add(STOPPER, STOPPER,         -0.05);

        add(BALL_PLAYING_CB, STOPPER,         +0.06);
        add(BALL_PLAYING_CB, WING_BACK,       +0.04);
        add(BALL_PLAYING_CB, SWEEPER_KEEPER,  +0.05); // Constructie de jos
        add(BALL_PLAYING_CB, BALL_PLAYING_CB, -0.04);

        add(WING_BACK, SPEEDSTER,   +0.05); // Banda rapida
        add(WING_BACK, BOX_TO_BOX,  +0.04);
        add(WING_BACK, TARGET_MAN,  +0.10); // Centrari

        // ================= GOALKEEPER =================
        add(SWEEPER_KEEPER, BALL_PLAYING_CB, +0.05);
        add(SWEEPER_KEEPER, WING_BACK,       +0.04); // Contratac rapid

        add(CLASSIC_GK, STOPPER, +0.04);
    }

    private RoleSynergy() {}

    /**
     * Helper pentru a popula harta curat.
     */
    private static void add(PlayerArchetype a, PlayerArchetype b, double score) {
        SYNERGY.computeIfAbsent(a, k -> new EnumMap<>(PlayerArchetype.class)).put(b, score);
    }

    /**
     * Returns chemistry bonus/penalty. Symmetric.
     */
    public static double get(PlayerArchetype a, PlayerArchetype b) {
        if (a == null || b == null) return 0.0;

        // Cautare directa A -> B
        var mapA = SYNERGY.get(a);
        if (mapA != null && mapA.containsKey(b)) {
            return mapA.get(b);
        }

        // Cautare inversa B -> A (Simetrie)
        var mapB = SYNERGY.get(b);
        if (mapB != null && mapB.containsKey(a)) {
            return mapB.get(a);
        }

        return 0.0;
    }
}