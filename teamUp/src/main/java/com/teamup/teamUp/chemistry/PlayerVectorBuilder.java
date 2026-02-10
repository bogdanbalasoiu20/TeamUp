package com.teamup.teamUp.chemistry;

import com.teamup.teamUp.model.entity.PlayerBehaviorStats;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import org.springframework.stereotype.Component;

//transforma datele brute in limbajul AI(normalizeaza)
@Component
public class PlayerVectorBuilder {
    private static final double MAX_BEHAVIOR_VAL = 99.0;

    public double[] buildBehavior(PlayerBehaviorStats behavior) {
        return new double[]{
                normBehavior(behavior.getFairPlay()),
                normBehavior(behavior.getCompetitiveness()),
                normBehavior(behavior.getCommunication()),
                normBehavior(behavior.getFun()),
                normBehavior(behavior.getSelfishness()),
                normBehavior(behavior.getAggressiveness())
        };
    }


    //[0-99] -> [0,1]
    private double normBehavior(double v) {
        v = Math.max(0.0, Math.min(MAX_BEHAVIOR_VAL, v));
        return v / MAX_BEHAVIOR_VAL;
    }
}
