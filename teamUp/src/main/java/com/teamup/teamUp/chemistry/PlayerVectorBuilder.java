package com.teamup.teamUp.chemistry;

import com.teamup.teamUp.model.entity.PlayerBehaviorStats;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import org.springframework.stereotype.Component;

//transforma datele brute in limbajul AI(normalizeaza)
@Component
public class PlayerVectorBuilder {
    private static final double MIN_FIFA = 68.0;
    private static final double MAX_FIFA = 99.0;

    public double[] buildSkill(PlayerCardStats stats) {
        return new double[]{
                normFifa(stats.getPace()),
                normFifa(stats.getShooting()),
                normFifa(stats.getPassing()),
                normFifa(stats.getDribbling()),
                normFifa(stats.getDefending()),
                normFifa(stats.getPhysical()),
        };
    }


    public double[] buildBehavior(PlayerBehaviorStats behavior) {
        return new double[]{
                normBehavior(behavior.getFairPlay()),
                normBehavior(behavior.getCompetitiveness()),
                normBehavior(behavior.getCommunication()),
                normBehavior(behavior.getFun()),
                normBehavior(behavior.getAdaptability()), //de modificat mai tarziu in cod din adaptability in selfishness
                normBehavior(behavior.getReliability()) //si aici din reliability in aggressivness
        };
    }



    //aduce stats-urile din intervalul 68-99 la intervalul [0,1]
    private double normFifa(double value) {
        value = Math.max(MIN_FIFA, Math.min(MAX_FIFA, value));
        return (value - MIN_FIFA) / (MAX_FIFA - MIN_FIFA);
    }

    //[0-99] -> [0,1]
    private double normBehavior(double v) {
        v = Math.max(0.0, Math.min(MAX_FIFA, v));
        return v / MAX_FIFA;
    }
}
