package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.ReasonType;
import com.teamup.teamUp.chemistry.dto.AdjustmentResult;
import com.teamup.teamUp.chemistry.dto.ChemistryReasons;
import com.teamup.teamUp.chemistry.dto.ChemistryResult;
import com.teamup.teamUp.chemistry.mapper.ChemistryScoreMapper;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.entity.PlayerBehaviorStats;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.repository.MatchParticipantRepository;
import com.teamup.teamUp.repository.PlayerBehaviorStatsRepository;
import com.teamup.teamUp.repository.PlayerCardStatsRepository;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ChemistryServiceImpl implements ChemistryService {

    private final PlayerBehaviorStatsRepository behaviorRepo;
    private final PlayerCardStatsRepository cardRepo;
    private final UserRepository userRepo;
    private final MatchParticipantRepository matchRepo;

    private final PlayerSimilarityService playerSimilarityService;
    private final ChemistryAdjustmentService chemistryAdjustmentService;
    private final ChemistryScoreMapper scoreMapper;

    public ChemistryServiceImpl(
            PlayerBehaviorStatsRepository behaviorRepo,
            PlayerCardStatsRepository cardRepo,
            UserRepository userRepo,
            MatchParticipantRepository matchRepo,
            PlayerSimilarityService playerSimilarityService,
            ChemistryAdjustmentService chemistryAdjustmentService,
            ChemistryScoreMapper scoreMapper) {
        this.behaviorRepo = behaviorRepo;
        this.cardRepo = cardRepo;
        this.userRepo = userRepo;
        this.matchRepo = matchRepo;
        this.playerSimilarityService = playerSimilarityService;
        this.chemistryAdjustmentService = chemistryAdjustmentService;
        this.scoreMapper = scoreMapper;
    }

    @Override
    public ChemistryResult compute(UUID userA, UUID userB) {
        User uA = userRepo.findById(userA).orElseThrow(() -> new NotFoundException("User A not found"));
        User uB = userRepo.findById(userB).orElseThrow(() -> new NotFoundException("User B not found"));

        PlayerCardStats cardA = cardRepo.findById(userA).orElseThrow(() -> new NotFoundException("Card A not found"));
        PlayerCardStats cardB = cardRepo.findById(userB).orElseThrow(() -> new NotFoundException("Card B not found"));

        PlayerBehaviorStats behA = behaviorRepo.findByUser_Id(userA).orElseThrow(() -> new NotFoundException("Behavior A not found"));
        PlayerBehaviorStats behB = behaviorRepo.findByUser_Id(userB).orElseThrow(() -> new NotFoundException("Behavior B not found"));

        // numaram meciurile celor 2 useri impreuna
        int matchesTogether = matchRepo.countMatchesTogether(userA, userB);

        int matchesPlayedUserA = matchRepo.countByUser_Id(userA);
        int matchesPlayedUserB = matchRepo.countByUser_Id(userB);

        //calculez similaritatea sociala dintre cei 2 useri
        double similarity = playerSimilarityService.calculate(behA, behB);

        //ajustez scorul de chemistry
        AdjustmentResult adjusted = chemistryAdjustmentService.adjust(similarity, cardA, uA.getPosition(), behA, cardB, uB.getPosition(), behB, matchesTogether, matchesPlayedUserA, matchesPlayedUserB);

        int score = scoreMapper.toScore(adjusted.adjustmentSimilarity());
        List<ChemistryReasons> reasons = new ArrayList<>(adjusted.reasons());

        if (similarity > 0.75) {
            reasons.add(new ChemistryReasons("Similar behavior", ReasonType.POSITIVE));
        }

        return new ChemistryResult(score, similarity, reasons, adjusted.roleA(), adjusted.roleB());
    }
}