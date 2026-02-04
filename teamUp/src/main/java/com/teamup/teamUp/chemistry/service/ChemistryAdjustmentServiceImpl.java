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
            adjusted *= positionFactor;

            if (positionFactor <= 0.8) {
                reasons.add("Low positional compatibility");
            } else if (positionFactor < 1.0) {
                reasons.add("Moderate positional compatibility");
            }
        }

        PlayerBehaviorStats behaviorA = playerBehaviorStatsRepository.findByUser_Id(userA).orElseThrow(()->new NotFoundException("User behavior not found"));
        PlayerBehaviorStats behaviorB = playerBehaviorStatsRepository.findByUser_Id(userB).orElseThrow(()->new NotFoundException("User behavior not found"));

        //penalizari pentru behavior slab pe pozitii unde conteaza
        if (posA == Position.MIDFIELDER && behaviorA.getCommunication() < 40) {
            adjusted -= 0.05;
            reasons.add("Low communication for midfielder role");
        }

        if (posA == Position.FORWARD && behaviorA.getSelfishness() > 75) {
            adjusted -= 0.08;
            reasons.add("High selfishness for goalkeeper");
        }


        //clamp final pentru siguranta
        adjusted = Math.max(0.0, Math.min(1.0, adjusted));

        return new AdjustmentResult(adjusted, reasons);
    }
}

