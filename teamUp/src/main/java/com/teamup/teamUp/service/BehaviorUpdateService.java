package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.entity.PlayerBehaviorRating;
import com.teamup.teamUp.model.entity.PlayerBehaviorStats;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.repository.PlayerBehaviorRatingRepository;
import com.teamup.teamUp.repository.PlayerBehaviorStatsRepository;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BehaviorUpdateService {

    private static final double BASE_ALPHA = 0.30;
    private static final int EXPERIENCE_FEEDBACK_CAP = 10;
    private static final double MAX_DELTA = 3.0;
    private static final double MIN = 0.0;
    private static final double MAX = 99.0;

    private final PlayerBehaviorRatingRepository ratingRepo;
    private final PlayerBehaviorStatsRepository statsRepo;
    private final UserRepository userRepository;

    public  BehaviorUpdateService(PlayerBehaviorRatingRepository ratingRepo, PlayerBehaviorStatsRepository statsRepo, UserRepository userRepository) {
        this.ratingRepo = ratingRepo;
        this.statsRepo = statsRepo;
        this.userRepository = userRepository;
    }

    public void updateAfterMatch(UUID matchId) {

        Map<UUID, List<PlayerBehaviorRating>> ratingsByUser = ratingRepo.findByIdMatchId(matchId).stream()
                        .collect(Collectors.groupingBy(r -> r.getRatedUser().getId()));

        for (UUID userId : ratingsByUser.keySet()) {
            User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

            List<PlayerBehaviorRating> ratings = ratingsByUser.get(userId);

            if (ratings == null || ratings.isEmpty()) continue;

            PlayerBehaviorStats stats = statsRepo.findByUser_Id(userId).orElseGet(() -> {
                        PlayerBehaviorStats s = createInitialBehaviorStats(user);
                        return statsRepo.save(s);});


            Map<String, Double> matchAvg = calculateMatchAverages(ratings);

            int numVotes = ratings.size();
            int feedbackCount = stats.getFeedbackCount();
            double alpha = computeAlpha(numVotes, feedbackCount);

            applyEma(stats, matchAvg, alpha);

            stats.setFeedbackCount(feedbackCount + 1);
            statsRepo.save(stats);
        }
    }

    private double computeAlpha(int numVotes, int feedbackCount) {
        double voteFactor = Math.min(1.0, Math.log(numVotes + 1) / Math.log(6));
        double experienceFactor = Math.min(1.0, feedbackCount / (double) EXPERIENCE_FEEDBACK_CAP);
        double alpha = BASE_ALPHA + 0.10 * voteFactor + 0.15 * (1 - experienceFactor);
        return Math.min(0.35, Math.max(0.05, alpha));
    }

    private Map<String, Double> calculateMatchAverages(List<PlayerBehaviorRating> ratings) {
        Map<String, Double> avg = new java.util.HashMap<>();

        avg.put("fairPlay", ratings.stream()
                .map(PlayerBehaviorRating::getFairPlay)
                .filter(v -> v != null)
                .mapToInt(Short::intValue)
                .average().orElse(Double.NaN));

        avg.put("communication", ratings.stream()
                .map(PlayerBehaviorRating::getCommunication)
                .filter(v -> v != null)
                .mapToInt(Short::intValue)
                .average().orElse(Double.NaN));

        avg.put("fun", ratings.stream()
                .map(PlayerBehaviorRating::getFun)
                .filter(v -> v != null)
                .mapToInt(Short::intValue)
                .average().orElse(Double.NaN));

        avg.put("competitiveness", ratings.stream()
                .map(PlayerBehaviorRating::getCompetitiveness)
                .filter(v -> v != null)
                .mapToInt(Short::intValue)
                .average().orElse(Double.NaN));

        avg.put("selfishness", ratings.stream()
                .map(PlayerBehaviorRating::getSelfishness)
                .filter(v -> v != null)
                .mapToInt(Short::intValue)
                .average().orElse(Double.NaN));

        avg.put("aggressiveness", ratings.stream()
                .map(PlayerBehaviorRating::getAggressiveness)
                .filter(v -> v != null)
                .mapToInt(Short::intValue)
                .average().orElse(Double.NaN));

        return avg;
    }

    private void applyEma(PlayerBehaviorStats stats, Map<String, Double> avg, double alpha) {
        avg.forEach((stat, matchValue) -> {
            if (Double.isNaN(matchValue)) {
                return;
            }

            double oldValue = getStat(stats, stat);
            double ema = alpha * matchValue + (1 - alpha) * oldValue;
            double clamped = clamp(ema, oldValue);

            setStat(stats, stat, clamped);
        });
    }

    private double getStat(PlayerBehaviorStats stats, String stat) {
        return switch (stat) {
            case "fairPlay" -> stats.getFairPlay();
            case "communication" -> stats.getCommunication();
            case "fun" -> stats.getFun();
            case "competitiveness" -> stats.getCompetitiveness();
            case "selfishness" -> stats.getSelfishness();
            case "aggressiveness" -> stats.getAggressiveness();
            default -> throw new IllegalArgumentException("Unknown stat: " + stat);
        };
    }

    private void setStat(PlayerBehaviorStats stats, String stat, double value) {
        switch (stat) {
            case "fairPlay" -> stats.setFairPlay(value);
            case "communication" -> stats.setCommunication(value);
            case "fun" -> stats.setFun(value);
            case "competitiveness" -> stats.setCompetitiveness(value);
            case "selfishness" -> stats.setSelfishness(value);
            case "aggressiveness" -> stats.setAggressiveness(value);
            default -> throw new IllegalArgumentException("Unknown stat: " + stat);
        }
    }

    private double clamp(double newValue, double oldValue) {
        double deltaClamped = Math.max(oldValue - MAX_DELTA, Math.min(oldValue + MAX_DELTA, newValue));

        return Math.max(MIN, Math.min(MAX, deltaClamped));
    }


    private PlayerBehaviorStats createInitialBehaviorStats(User user) {
        return PlayerBehaviorStats.builder()
                .user(user)
                .fairPlay(70.0)
                .communication(70.0)
                .fun(70.0)
                .competitiveness(70.0)
                .selfishness(70.0)
                .aggressiveness(70.0)
                .feedbackCount(0)
                .build();
    }




}

