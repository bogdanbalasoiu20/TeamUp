package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.dto.AdjustmentResult;
import com.teamup.teamUp.chemistry.dto.ChemistryResult;
import com.teamup.teamUp.chemistry.mapper.ChemistryScoreMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//servicul final, imbina celelalte servicii
@Service
public class ChemistryServiceImpl implements ChemistryService {

    private final PlayerSimilarityService playerSimilarityService;
    private final ChemistryAdjustmentService chemistryAdjustmentService;
    private final ChemistryScoreMapper scoreMapper;

    public ChemistryServiceImpl(PlayerSimilarityService similarityService, ChemistryAdjustmentService chemistryAdjustmentService, ChemistryScoreMapper scoreMapper) {
        this.playerSimilarityService = similarityService;
        this.chemistryAdjustmentService = chemistryAdjustmentService;
        this.scoreMapper = scoreMapper;
    }

    @Override
    public ChemistryResult compute(UUID userA, UUID userB) {
        double similarity = playerSimilarityService.similarity(userA, userB); //intoarce similaritatea intre cei 2 useri

        AdjustmentResult adjusted = chemistryAdjustmentService.adjust(userA, userB, similarity);  //ajusteaza similaritatea, o face mai realista

        int score = scoreMapper.toScore(adjusted.adjustmentSimilarity()); //mapez scorul pentru user

        List<String> reasons = new ArrayList<>(adjusted.reasons());  //lista de motive

        if(similarity>0.75){
            reasons.add("Similar play style and behavior");
        }

        return new ChemistryResult(score, similarity, reasons);
    }
}
