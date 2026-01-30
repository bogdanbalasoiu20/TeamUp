package com.teamup.teamUp.chemistry.service;


import com.teamup.teamUp.chemistry.dto.AdjustmentResult;
import com.teamup.teamUp.model.entity.MatchParticipant;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.repository.MatchParticipantRepository;
import com.teamup.teamUp.repository.MatchRepository;
import com.teamup.teamUp.repository.PlayerCardStatsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//ajustare pentru raspunsul oferit de AI pentru similaritatea dintre 2 useri(chiar daca seama inca pot exista diferente mari care trebuie evitate)
//ofera realism
@Service
public class ChemistryAdjustmentServiceImpl implements ChemistryAdjustmentService {

    private final PlayerCardStatsRepository cardStatsRepository;
    private final MatchParticipantRepository matchParticipantRepository;

    public ChemistryAdjustmentServiceImpl(PlayerCardStatsRepository cardStatsRepository, MatchParticipantRepository matchParticipantRepository) {
        this.cardStatsRepository = cardStatsRepository;
        this.matchParticipantRepository = matchParticipantRepository;
    }

    @Override
    public AdjustmentResult adjust(UUID userA, UUID userB, double similarity) {

        //pornesc de la rezultatul AI-ului
        double adjusted = similarity;
        List<String> reasons = new ArrayList<>();

        //iau ratingurile
        Double ratingA = cardStatsRepository.getOverall(userA);
        Double ratingB = cardStatsRepository.getOverall(userB);

        if (ratingA == null || ratingB == null) {
            return new AdjustmentResult(similarity, List.of("Insufficient data"));
        }

        //penalizare pentru diferenta prea mare intre ratinguri
        double diff = Math.abs(ratingA - ratingB);

        //penalizare dinamica
        if (diff > 15) {
            double penalty = Math.min(0.15, (diff - 15) * 0.01);
            adjusted -= penalty;
            reasons.add("Large level difference");
        }

        //nr de meciuri jucate impreuna
        int matchesTogether = matchParticipantRepository.countMatchesTogether(userA, userB);

        //daca avem macar un meci -> bonus dinamic
        if (matchesTogether > 0) {
            double bonus = Math.min(0.10, matchesTogether * 0.01);
            adjusted += bonus;
            reasons.add("Played together before");
        }

        //clamp final pentru siguranta
        adjusted = Math.max(0.0, Math.min(1.0, adjusted));

        return new AdjustmentResult(adjusted, reasons);
    }
}

