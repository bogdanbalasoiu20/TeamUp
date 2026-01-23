package com.teamup.teamUp.service;

import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.entity.PlayerCardStatsHistory;
import com.teamup.teamUp.model.entity.PlayerRating;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.Position;
import com.teamup.teamUp.repository.PlayerCardStatsHistoryRepository;
import com.teamup.teamUp.repository.PlayerCardStatsRepository;
import com.teamup.teamUp.repository.PlayerRatingRepository;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RatingUpdateService {

    private static final double BASE_ALPHA = 0.15;
    private static final int EXPERIENCE_MATCHES_CAP = 10;
    private static final double MAX_DELTA = 2.0;
    private static final double MIN_RATING = 68.0;
    private static final double MAX_RATING = 99.0;

    private static final Map<Position, Map<String, Double>> POSITION_WEIGHTS = Map.of(

            // Ponderi atacant
            Position.FORWARD, Map.of(
                    "shooting", 0.30,
                    "dribbling", 0.20,
                    "pace", 0.20,
                    "passing", 0.15,
                    "physical", 0.10,
                    "defending", 0.05
            ),

            // Ponderi mijlocas
            Position.MIDFIELDER, Map.of(
                    "passing", 0.30,
                    "dribbling", 0.20,
                    "defending", 0.20,
                    "pace", 0.15,
                    "physical", 0.10,
                    "shooting", 0.05
            ),

            // Ponderi fundas
            Position.DEFENDER, Map.of(
                    "defending", 0.35,
                    "physical", 0.25,
                    "pace", 0.15,
                    "passing", 0.15,
                    "dribbling", 0.05,
                    "shooting", 0.05
            ),

            // Ponderi portar
            Position.GOALKEEPER, Map.of(
                    "gkReflexes", 0.30,
                    "gkPositioning", 0.20,
                    "gkHandling", 0.20,
                    "gkDiving", 0.15,
                    "gkKicking", 0.10,
                    "gkSpeed", 0.05
            )
    );



    private final PlayerRatingRepository ratingRepo;
    private final PlayerCardStatsRepository cardRepo;
    private final PlayerCardStatsHistoryRepository historyRepo;
    private final UserRepository userRepo;

    public RatingUpdateService(PlayerRatingRepository ratingRepo, PlayerCardStatsRepository cardRepo, PlayerCardStatsHistoryRepository historyRepo, UserRepository userRepo) {
        this.ratingRepo = ratingRepo;
        this.cardRepo = cardRepo;
        this.historyRepo = historyRepo;
        this.userRepo = userRepo;
    }

    public void updateAfterMatch(UUID matchId) {
        // Grupez toate ratingurile pe jucator evaluat
        Map<UUID, List<PlayerRating>> ratingsByUser = ratingRepo.findByIdMatchId(matchId).stream()
                        .collect(Collectors.groupingBy(r -> r.getRatedUser().getId()));


        for (UUID userId : ratingsByUser.keySet()) {

            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("User not found"));

            List<PlayerRating> ratings = ratingsByUser.get(userId);

            if (ratings == null || ratings.isEmpty()) {
                continue;
            }

            //Media pe meci
            Map<String, Double> matchAvg = calculateMatchAverages(user, ratings);

            // Card curent (sau default)
            PlayerCardStats card = cardRepo.findById(userId).orElseGet(() -> createInitialCard(userId, user));

            int numVotes = ratings.size();
            int matchesPlayed = (int) historyRepo.countByUserId(userId);
            double alpha = computeAlpha(numVotes, matchesPlayed);

            // EMA + clamp
            applyEma(card, matchAvg, alpha, user.getPosition());

            card.setLastUpdated(Instant.now());
            cardRepo.save(card);

            // Istoric
            historyRepo.save(snapshot(card, matchId));
        }
    }

    private Map<String, Double> calculateMatchAverages(User user, List<PlayerRating> ratings) {
        Map<String, Double> avg = new HashMap<>();

        if (user.getPosition() == Position.GOALKEEPER) {
            avg.put("gkDiving", ratings.stream().mapToInt(r -> r.getGkDiving()).average().orElse(0));
            avg.put("gkHandling", ratings.stream().mapToInt(r -> r.getGkHandling()).average().orElse(0));
            avg.put("gkKicking", ratings.stream().mapToInt(r -> r.getGkKicking()).average().orElse(0));
            avg.put("gkReflexes", ratings.stream().mapToInt(r -> r.getGkReflexes()).average().orElse(0));
            avg.put("gkSpeed", ratings.stream().mapToInt(r -> r.getGkSpeed()).average().orElse(0));
            avg.put("gkPositioning", ratings.stream().mapToInt(r -> r.getGkPositioning()).average().orElse(0));
        } else {
            avg.put("pace", ratings.stream().mapToInt(r -> r.getPace()).average().orElse(0));
            avg.put("shooting", ratings.stream().mapToInt(r -> r.getShooting()).average().orElse(0));
            avg.put("passing", ratings.stream().mapToInt(r -> r.getPassing()).average().orElse(0));
            avg.put("defending", ratings.stream().mapToInt(r -> r.getDefending()).average().orElse(0));
            avg.put("dribbling", ratings.stream().mapToInt(r -> r.getDribbling()).average().orElse(0));
            avg.put("physical", ratings.stream().mapToInt(r -> r.getPhysical()).average().orElse(0));
        }

        return avg;
    }

    private void applyEma(PlayerCardStats card, Map<String, Double> avg, double alpha,Position position) {
        avg.forEach((stat, matchValue) -> {
            Double oldValue = getStat(card, stat);
            double ema = alpha * matchValue + (1 - alpha) * oldValue;
            double clamped = clamp(ema, oldValue);
            setStat(card, stat, clamped);
        });

        card.setOverallRating(calculateOverall(card, position));
    }

    private double clamp(double newValue, double oldValue) {
        double deltaClamped = Math.max(
                oldValue - MAX_DELTA,
                Math.min(oldValue + MAX_DELTA, newValue)
        );

        return Math.max(MIN_RATING, Math.min(MAX_RATING, deltaClamped));
    }



    private PlayerCardStatsHistory snapshot(PlayerCardStats card, UUID matchId) {
        return PlayerCardStatsHistory.builder()
                .userId(card.getUserId())
                .matchId(matchId)
                .pace(card.getPace())
                .shooting(card.getShooting())
                .passing(card.getPassing())
                .defending(card.getDefending())
                .dribbling(card.getDribbling())
                .physical(card.getPhysical())
                .gkDiving(card.getGkDiving())
                .gkHandling(card.getGkHandling())
                .gkKicking(card.getGkKicking())
                .gkReflexes(card.getGkReflexes())
                .gkSpeed(card.getGkSpeed())
                .gkPositioning(card.getGkPositioning())
                .overallRating(card.getOverallRating())
                .recordedAt(Instant.now())
                .build();
    }


    private PlayerCardStats createInitialCard(UUID userId, User user) {
        boolean isGk = user.getPosition() == Position.GOALKEEPER;

        PlayerCardStats card = PlayerCardStats.builder()
                .userId(userId)
                .lastUpdated(Instant.now())
                .overallRating(68.0)
                .build();

        if (isGk) {
            card.setGkDiving(68.0);
            card.setGkHandling(68.0);
            card.setGkKicking(68.0);
            card.setGkReflexes(68.0);
            card.setGkSpeed(68.0);
            card.setGkPositioning(68.0);
        } else {
            card.setPace(68.0);
            card.setShooting(68.0);
            card.setPassing(68.0);
            card.setDefending(68.0);
            card.setDribbling(68.0);
            card.setPhysical(68.0);
        }

        return card;
    }

    private Double getStat(PlayerCardStats card, String stat) {
        return switch (stat) {
            case "pace" -> card.getPace();
            case "shooting" -> card.getShooting();
            case "passing" -> card.getPassing();
            case "defending" -> card.getDefending();
            case "dribbling" -> card.getDribbling();
            case "physical" -> card.getPhysical();
            case "gkDiving" -> card.getGkDiving();
            case "gkHandling" -> card.getGkHandling();
            case "gkKicking" -> card.getGkKicking();
            case "gkReflexes" -> card.getGkReflexes();
            case "gkSpeed" -> card.getGkSpeed();
            case "gkPositioning" -> card.getGkPositioning();
            default -> throw new IllegalArgumentException("Unknown stat: " + stat);
        };
    }

    private void setStat(PlayerCardStats card, String stat, double value) {
        switch (stat) {
            case "pace" -> card.setPace(value);
            case "shooting" -> card.setShooting(value);
            case "passing" -> card.setPassing(value);
            case "defending" -> card.setDefending(value);
            case "dribbling" -> card.setDribbling(value);
            case "physical" -> card.setPhysical(value);
            case "gkDiving" -> card.setGkDiving(value);
            case "gkHandling" -> card.setGkHandling(value);
            case "gkKicking" -> card.setGkKicking(value);
            case "gkReflexes" -> card.setGkReflexes(value);
            case "gkSpeed" -> card.setGkSpeed(value);
            case "gkPositioning" -> card.setGkPositioning(value);
            default -> throw new IllegalArgumentException("Unknown stat: " + stat);
        }
    }


    private double calculateOverall(PlayerCardStats card, Position position) {

        Map<String, Double> weights = POSITION_WEIGHTS.get(position);

        if (weights == null) {
            throw new IllegalStateException("No weights defined for position: " + position);
        }

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            String stat = entry.getKey();
            double weight = entry.getValue();

            Double statValue = getStat(card, stat);
            if (statValue == null) continue;

            weightedSum += statValue * weight;
            totalWeight += weight;
        }

        return totalWeight > 0 ? weightedSum / totalWeight : MIN_RATING;
    }



    private double computeAlpha(int numVotes, int matchesPlayed) {

        double voteFactor = Math.min(1.0, Math.log(numVotes + 1) / Math.log(6));
        double experienceFactor = Math.min(1.0, matchesPlayed / (double) EXPERIENCE_MATCHES_CAP);
        double alpha = BASE_ALPHA + 0.15 * voteFactor + 0.20 * (1 - experienceFactor);
        return Math.min(0.5, Math.max(0.1, alpha));
    }


}




