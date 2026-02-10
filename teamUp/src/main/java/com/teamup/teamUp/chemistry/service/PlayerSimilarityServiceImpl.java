package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.CosineSimilarity;
import com.teamup.teamUp.chemistry.PlayerVectorBuilder;
import com.teamup.teamUp.model.entity.PlayerBehaviorStats;
import org.springframework.stereotype.Service;

@Service
public class PlayerSimilarityServiceImpl implements PlayerSimilarityService {

    private final PlayerVectorBuilder vectorBuilder;

    public PlayerSimilarityServiceImpl(PlayerVectorBuilder vectorBuilder) {
        this.vectorBuilder = vectorBuilder;
    }

    //calculez vectorii pentru behaviorul celor 2 useri
    @Override
    public double calculate(PlayerBehaviorStats behaviorA, PlayerBehaviorStats behaviorB) {

        double[] vecA = vectorBuilder.buildBehavior(behaviorA);
        double[] vecB = vectorBuilder.buildBehavior(behaviorB);

        return CosineSimilarity.compute(vecA, vecB);
    }
}