package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.CosineSimilarity;
import com.teamup.teamUp.chemistry.PlayerVectorBuilder;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.entity.PlayerBehaviorStats;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.repository.PlayerBehaviorStatsRepository;
import com.teamup.teamUp.repository.PlayerCardStatsRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

//Servicul pentru AI-ul efectiv
@Service
public class PlayerSimilarityServiceImpl implements PlayerSimilarityService {
    private static final double SKILL_WEIGHT = 0.3;
    private static final double BEHAVIOR_WEIGHT = 0.7;

    private final PlayerCardStatsRepository cardStatsRepository;
    private final PlayerBehaviorStatsRepository  behaviorStatsRepository;
    private final PlayerVectorBuilder vectorBuilder;

    public PlayerSimilarityServiceImpl(PlayerCardStatsRepository cardStatsRepository, PlayerBehaviorStatsRepository behaviorStatsRepository, PlayerVectorBuilder vectorBuilder) {
        this.cardStatsRepository = cardStatsRepository;
        this.behaviorStatsRepository = behaviorStatsRepository;
        this.vectorBuilder = vectorBuilder;
    }

    @Override
    public double similarity(UUID userA, UUID userB) {
        //stats-urile de pe cardul de fifa pentru cei doi useri
        PlayerCardStats statsA = cardStatsRepository.findById(userA).orElseThrow(() -> new NotFoundException("Card stats not found for user " + userA));
        PlayerCardStats statsB = cardStatsRepository.findById(userB).orElseThrow(() -> new NotFoundException("Card stats not found for user " + userB));

        //stats-urile de behavior
        PlayerBehaviorStats behaviorA = behaviorStatsRepository.findByUser_Id(userA).orElseThrow(() -> new NotFoundException("Behavior stats not found for user " + userA));
        PlayerBehaviorStats behaviorB = behaviorStatsRepository.findByUser_Id(userB).orElseThrow(() -> new NotFoundException("Behavior stats not found for user " + userB));

        //construiesc vectorii de skills
        double[] skillA = vectorBuilder.buildSkill(statsA);
        double[] skillB = vectorBuilder.buildSkill(statsB);

        //construiesc vectorii de behavior
        double[] behaviorA_vec = vectorBuilder.buildBehavior(behaviorA);
        double[] behaviorB_vec = vectorBuilder.buildBehavior(behaviorB);

        //calculez similaritatile
        double skillSimilarity = CosineSimilarity.compute(skillA, skillB);
        double behaviorSimilarity = CosineSimilarity.compute(behaviorA_vec, behaviorB_vec);

        return SKILL_WEIGHT * skillSimilarity + BEHAVIOR_WEIGHT * behaviorSimilarity;
    }


}
