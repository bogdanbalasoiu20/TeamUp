package com.teamup.teamUp.chemistry.service;


import com.teamup.teamUp.chemistry.dto.AdjustmentResult;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.entity.PlayerBehaviorStats;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.repository.MatchParticipantRepository;
import com.teamup.teamUp.repository.PlayerBehaviorStatsRepository;
import com.teamup.teamUp.repository.PlayerCardStatsRepository;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.teamup.teamUp.chemistry.PositionSynergy;
import com.teamup.teamUp.model.enums.Position;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//ajustare pentru raspunsul oferit de AI pentru similaritatea dintre 2 useri(chiar daca seama inca pot exista diferente mari care trebuie evitate)
//ofera realism
@Service
public class ChemistryAdjustmentServiceImpl implements ChemistryAdjustmentService {

    private final PlayerCardStatsRepository cardStatsRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final UserRepository userRepository;
    private final PlayerBehaviorStatsRepository playerBehaviorStatsRepository;

    public ChemistryAdjustmentServiceImpl(PlayerCardStatsRepository cardStatsRepository, MatchParticipantRepository matchParticipantRepository, UserRepository userRepository, PlayerBehaviorStatsRepository playerBehaviorStatsRepository) {
        this.cardStatsRepository = cardStatsRepository;
        this.matchParticipantRepository = matchParticipantRepository;
        this.userRepository = userRepository;
        this.playerBehaviorStatsRepository = playerBehaviorStatsRepository;
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

        Position posA = userRepository.findById(userA).orElseThrow(()->new NotFoundException("User not found")).getPosition();
        Position posB = userRepository.findById(userB).orElseThrow(()->new NotFoundException("User not found")).getPosition();

        if (posA != null && posB != null) {
            double positionFactor = PositionSynergy.get(posA, posB);
            adjusted += (positionFactor - 1.0) * 0.2;

            if (positionFactor <= 0.8) {
                reasons.add("Low positional compatibility");
            } else if (positionFactor < 1.0) {
                reasons.add("Moderate positional compatibility");
            }
        }

        PlayerBehaviorStats behaviorA = playerBehaviorStatsRepository.findByUser_Id(userA).orElseThrow(()->new NotFoundException("User behavior not found"));
        PlayerBehaviorStats behaviorB = playerBehaviorStatsRepository.findByUser_Id(userB).orElseThrow(()->new NotFoundException("User behavior not found"));

        /**
         * Computes chemistry from the perspective of userA.
         * Used as a compatibility signal, not a global symmetric score.
         */


        //reguli pentru portar
        if (posA == Position.GOALKEEPER && behaviorA.getAggressiveness() > 70) {
            adjusted -= 0.06;
            reasons.add("High aggressiveness for goalkeeper");
        }

        if (posA == Position.GOALKEEPER && behaviorA.getCommunication() < 40) {
            adjusted -= 0.05;
            reasons.add("Low communication for goalkeeper");
        }

        if (posA == Position.GOALKEEPER && behaviorA.getFairPlay() < 40) {
            adjusted -= 0.04;
            reasons.add("Low fair play for goalkeeper");
        }

        //reguli pentru fundas
        if (posA == Position.DEFENDER && behaviorA.getAggressiveness() > 80) {
            adjusted -= 0.07;
            reasons.add("Too aggressive for defender role");
        }

        if (posA == Position.DEFENDER && behaviorA.getCommunication() < 45) {
            adjusted -= 0.04;
            reasons.add("Low communication for defender role");
        }

        if (posA == Position.DEFENDER && behaviorA.getCompetitiveness() < 40) {
            adjusted -= 0.03;
            reasons.add("Low competitiveness for defender role");
        }


        //reguli pentru mijlocas
        if (posA == Position.MIDFIELDER && behaviorA.getCommunication() < 45) {
            adjusted -= 0.06;
            reasons.add("Low communication for midfielder role");
        }

        if (posA == Position.MIDFIELDER && behaviorA.getSelfishness() > 65) {
            adjusted -= 0.05;
            reasons.add("High selfishness for midfielder role");
        }

        if (posA == Position.MIDFIELDER && behaviorA.getFun() < 40) {
            adjusted -= 0.03;
            reasons.add("Low team spirit for midfielder role");
        }


        //reguli pentru atacant
        if (posA == Position.FORWARD && behaviorA.getSelfishness() > 75) {
            adjusted -= 0.07;
            reasons.add("High selfishness for forward role");
        }

        if (posA == Position.FORWARD && behaviorA.getCommunication() < 35) {
            adjusted -= 0.04;
            reasons.add("Low communication for forward role");
        }

        if (posA == Position.FORWARD && behaviorA.getCompetitiveness() < 40) {
            adjusted -= 0.03;
            reasons.add("Low competitiveness for forward role");
        }

        //reguli pentru interactiune
        if (posA == Position.MIDFIELDER && posB == Position.FORWARD &&
                behaviorA.getCommunication() < 40 && behaviorB.getSelfishness() > 70) {
            adjusted -= 0.08;
            reasons.add("Poor midfield-forward cooperation");
        }

        if (posA == Position.DEFENDER && posB == Position.GOALKEEPER &&
                behaviorA.getCommunication() < 40) {
            adjusted -= 0.05;
            reasons.add("Weak defender-goalkeeper coordination");
        }


        //clamp final pentru siguranta
        adjusted = Math.max(0.0, Math.min(1.0, adjusted));

        return new AdjustmentResult(adjusted, reasons);
    }
}

