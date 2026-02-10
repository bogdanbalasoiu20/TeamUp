package com.teamup.teamUp.chemistry;

import com.teamup.teamUp.model.enums.Position;

import java.util.Map;

//se defineste importanta interactiunii in teren intre pozitii, cat de des interactioneaza intre ele
public class PositionSynergy {

    private static final Map<Position, Map<Position, Double>> SYNERGY = Map.of(
            Position.GOALKEEPER, Map.of(
                    Position.GOALKEEPER, 0.9,
                    Position.DEFENDER, 1.0,
                    Position.MIDFIELDER, 0.85,
                    Position.FORWARD, 0.75
            ),
            Position.DEFENDER, Map.of(
                    Position.GOALKEEPER, 1.0,
                    Position.DEFENDER, 0.9,
                    Position.MIDFIELDER, 1.0,
                    Position.FORWARD, 0.8
            ),
            Position.MIDFIELDER, Map.of(
                    Position.GOALKEEPER, 0.85,
                    Position.DEFENDER, 1.0,
                    Position.MIDFIELDER, 1.0,
                    Position.FORWARD, 1.0
            ),
            Position.FORWARD, Map.of(
                    Position.GOALKEEPER, 0.75,
                    Position.DEFENDER, 0.8,
                    Position.MIDFIELDER, 1.0,
                    Position.FORWARD, 0.9
            )
    );

    private PositionSynergy() {}

    public static double get(Position a, Position b) {
        return SYNERGY.getOrDefault(a, Map.of()).getOrDefault(b, 1.0);
    }
}
