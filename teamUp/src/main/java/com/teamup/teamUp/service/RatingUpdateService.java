package com.teamup.teamUp.service;

import com.teamup.teamUp.events.NotificationEvents;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.entity.*;
import com.teamup.teamUp.model.enums.EventType;
import com.teamup.teamUp.model.enums.MatchStatus;
import com.teamup.teamUp.model.enums.Position;
import com.teamup.teamUp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RatingUpdateService {

    private static final double BASE_ALPHA = 0.12;
    private static final int EXPERIENCE_MATCHES_CAP = 15;
    private static final double MAX_DELTA = 3;
    private static final double MIN_RATING = 55.0;
    private static final double MAX_RATING = 99.0;
    private static final double VISUAL_OVERALL_OFFSET = 2.5;

    private static final Map<Position, Map<String, Double>> POSITION_WEIGHTS = Map.of(

            // Ponderi atacant
            Position.FORWARD, Map.of(
                    "shooting", 0.35,
                    "dribbling", 0.20,
                    "pace", 0.20,
                    "passing", 0.10,
                    "physical", 0.15,
                    "defending", 0.0
            ),

            // Ponderi mijlocas
            Position.MIDFIELDER, Map.of(
                    "passing", 0.27,
                    "dribbling", 0.19,
                    "defending", 0.12,
                    "pace", 0.14,
                    "physical", 0.12,
                    "shooting", 0.16
            ),

            // Ponderi fundas
            Position.DEFENDER, Map.of(
                    "defending", 0.35,
                    "physical", 0.30,
                    "pace", 0.10,
                    "passing", 0.13,
                    "dribbling", 0.08,
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
    private final NotificationEvents notificationEvents;
    private final MatchRepository matchRepository;

    public RatingUpdateService(PlayerRatingRepository ratingRepo, PlayerCardStatsRepository cardRepo, PlayerCardStatsHistoryRepository historyRepo, UserRepository userRepo, NotificationEvents notificationEvents, MatchRepository matchRepository) {
        this.ratingRepo = ratingRepo;
        this.cardRepo = cardRepo;
        this.historyRepo = historyRepo;
        this.userRepo = userRepo;
        this.notificationEvents = notificationEvents;
        this.matchRepository = matchRepository;
    }

    public void updateAfterMatch(UUID matchId) {
        // Grupez toate ratingurile pe jucator evaluat
        Map<UUID, List<PlayerRating>> ratingsByUser = ratingRepo.findByIdMatchId(matchId).stream()
                        .collect(Collectors.groupingBy(r -> r.getRatedUser().getId()));

        Match match = matchRepository.findById(matchId).orElseThrow(()->new NotFoundException("Match not found"));


        for (UUID userId : ratingsByUser.keySet()) {

            User user = userRepo.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));

            List<PlayerRating> ratings = ratingsByUser.get(userId);

            if (ratings == null || ratings.isEmpty()) {
                continue;
            }

            //Media pe meci
            Map<String, Double> matchAvg = calculateMatchAverages(user, ratings);

            // Card curent (sau default)
            PlayerCardStats card = cardRepo.findById(userId).orElseGet(() -> createInitialCard(userId, user));

            double oldRating = card.getOverallRating();

            int numVotes = ratings.size();
            int matchesPlayed = (int) historyRepo.countByUserIdAndEventType(userId, EventType.OPEN_MATCH);
            double alpha = computeAlpha(numVotes, matchesPlayed);

            // EMA + clamp
            applyEma(card, matchAvg, alpha, user.getPosition());

            card.setLastUpdated(Instant.now());
            cardRepo.save(card);

            double newRating = card.getOverallRating();

            // Istoric
            historyRepo.save(snapshot(card, EventType.OPEN_MATCH, matchId));

            if (Math.round(oldRating) != Math.round(newRating)) {
                notificationEvents.ratingUpdatedAfterMatch(user, oldRating, newRating, match);
            }
        }
    }

    private double averageNonNull(
            List<PlayerRating> ratings,
            java.util.function.Function<PlayerRating, Short> extractor
    ) {
        return ratings.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .mapToInt(Short::intValue)
                .average()
                .orElse(Double.NaN);
    }


    private Map<String, Double> calculateMatchAverages(User user, List<PlayerRating> ratings) {
        Map<String, Double> avg = new HashMap<>();

        if (user.getPosition() == Position.GOALKEEPER) {
            avg.put("gkDiving",      averageNonNull(ratings, PlayerRating::getGkDiving));
            avg.put("gkHandling",    averageNonNull(ratings, PlayerRating::getGkHandling));
            avg.put("gkKicking",     averageNonNull(ratings, PlayerRating::getGkKicking));
            avg.put("gkReflexes",    averageNonNull(ratings, PlayerRating::getGkReflexes));
            avg.put("gkSpeed",       averageNonNull(ratings, PlayerRating::getGkSpeed));
            avg.put("gkPositioning", averageNonNull(ratings, PlayerRating::getGkPositioning));
        } else {
            avg.put("pace",       averageNonNull(ratings, PlayerRating::getPace));
            avg.put("shooting",   averageNonNull(ratings, PlayerRating::getShooting));
            avg.put("passing",    averageNonNull(ratings, PlayerRating::getPassing));
            avg.put("defending",  averageNonNull(ratings, PlayerRating::getDefending));
            avg.put("dribbling",  averageNonNull(ratings, PlayerRating::getDribbling));
            avg.put("physical",   averageNonNull(ratings, PlayerRating::getPhysical));
        }

        return avg;
    }


    private void applyEma(PlayerCardStats card, Map<String, Double> avg, double alpha, Position position) {
        avg.forEach((stat, matchValue) -> {
            if (Double.isNaN(matchValue)) {
                return;
            }
            Double oldValue = getStat(card, stat);
            double ema = alpha * matchValue + (1 - alpha) * oldValue;
            double clamped = clamp(ema, oldValue);
            setStat(card, stat, clamped);
        });

        card.setOverallRating(calculateOverall(card, position));
    }


    private double clamp(double newValue, double oldValue) {
        double deltaClamped = Math.max(oldValue - MAX_DELTA, Math.min(oldValue + MAX_DELTA, newValue));

        return Math.max(MIN_RATING, Math.min(MAX_RATING, deltaClamped));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }



    private PlayerCardStatsHistory snapshot(PlayerCardStats card, EventType eventType, UUID contextId) {
        return PlayerCardStatsHistory.builder()
                .userId(card.getUserId())
                .eventType(eventType)
                .contextId(contextId)
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


    public PlayerCardStats createInitialCard(UUID userId, User user) {

        Position position = user.getPosition();

        PlayerCardStats.PlayerCardStatsBuilder builder = PlayerCardStats.builder()
                .userId(userId)
                .lastUpdated(Instant.now());

        switch (position) {

            case FORWARD -> builder
                    .pace(68.0)
                    .shooting(70.0)
                    .passing(64.0)
                    .defending(58.0)
                    .dribbling(65.0)
                    .physical(64.0);

            case MIDFIELDER -> builder
                    .pace(65.0)
                    .shooting(64.0)
                    .passing(68.0)
                    .defending(62.0)
                    .dribbling(68.0)
                    .physical(60.0);

            case DEFENDER -> builder
                    .pace(63.0)
                    .shooting(58.0)
                    .passing(63.0)
                    .defending(70.0)
                    .dribbling(58.0)
                    .physical(68.0);

            case GOALKEEPER -> builder
                    .gkDiving(63.0)
                    .gkHandling(65.0)
                    .gkKicking(66.0)
                    .gkReflexes(68.0)
                    .gkSpeed(58.0)
                    .gkPositioning(63.0);
        }

        PlayerCardStats card = builder.build();

        card.setOverallRating(calculateOverall(card, position));

        historyRepo.save(snapshot(card, EventType.INITIAL, null));

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

        double overall = weightedSum / totalWeight;
        return Math.max(60.0, Math.min(MAX_RATING, overall+VISUAL_OVERALL_OFFSET));

    }



    private double computeAlpha(int numVotes, int matchesPlayed) {

        double voteFactor = Math.min(1.0, Math.log(numVotes + 1) / Math.log(6));
        double experienceFactor = Math.min(1.0, matchesPlayed / (double) EXPERIENCE_MATCHES_CAP);
        double alpha = BASE_ALPHA + 0.15 * voteFactor + 0.20 * (1 - experienceFactor);
        return Math.min(0.5, Math.max(0.1, alpha));
    }



    @Transactional
    public void recalculateOverallForUser(UUID userId, Position newPosition) {

        PlayerCardStats card = cardRepo.findById(userId).orElseThrow(() -> new IllegalStateException("Player card not found"));

        double newOverall = calculateOverall(card, newPosition);

        card.setOverallRating(newOverall);
        card.setLastUpdated(Instant.now());

        cardRepo.save(card);
    }


    public void updateAfterTournamentMatch(TournamentMatch match, List<TournamentMatchParticipant> participants) {
        if (match.getStatus() != MatchStatus.DONE) return;

        if (match.getOddsHome() == null || match.getOddsDraw() == null || match.getOddsAway() == null) {
            return;
        }

        double pHome = 1.0 / match.getOddsHome();
        double pDraw = 1.0 / match.getOddsDraw();
        double pAway = 1.0 / match.getOddsAway();

        double sum = pHome + pDraw + pAway;
        pHome /= sum;
        pDraw /= sum;
        pAway /= sum;

        double actualHome, actualAway;

        if (match.getScoreHome() > match.getScoreAway()) {
            actualHome = 1;
            actualAway = 0;
        } else if (match.getScoreHome().equals(match.getScoreAway())) {
            actualHome = 0.5;
            actualAway = 0.5;
        } else {
            actualHome = 0;
            actualAway = 1;
        }

        double SCALE = 1.2;

        double deltaHome = (actualHome - pHome) * SCALE;
        double deltaAway = (actualAway - pAway) * SCALE;

        int goalDiff = Math.abs(match.getScoreHome() - match.getScoreAway());
        double scoreFactor = 1 + Math.min(goalDiff, 3) * 0.15;

        deltaHome *= scoreFactor;
        deltaAway *= scoreFactor;

        for (TournamentMatchParticipant p : participants) {
            UUID userId = p.getUser().getId();
            Team team = p.getTeam();
            double delta = team.getId().equals(match.getHomeTeam().getId()) ? deltaHome : deltaAway;

            applyTournamentDelta(userId, delta, p.getUser().getPosition(), match);
        }
    }


    private void applyTournamentDelta(UUID userId, double delta, Position position, TournamentMatch match) {

        if (historyRepo.existsByUserIdAndContextId(userId, match.getId())) {
            return;
        }

        User user = userRepo.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));

        PlayerCardStats card = cardRepo.findById(userId).orElseThrow(() -> new IllegalStateException("Card not found"));

        double oldRating = card.getOverallRating();

        if (position == Position.GOALKEEPER) {
            applyGoalkeeperDelta(card, delta, match, user, oldRating);
            return;
        }

        double core = clamp(delta * 0.2, -0.3, 0.3);

        setStat(card, "pace", clamp(card.getPace() + core, card.getPace()));
        setStat(card, "shooting", clamp(card.getShooting() + core, card.getShooting()));
        setStat(card, "passing", clamp(card.getPassing() + core, card.getPassing()));
        setStat(card, "defending", clamp(card.getDefending() + core, card.getDefending()));
        setStat(card, "dribbling", clamp(card.getDribbling() + core, card.getDribbling()));
        setStat(card, "physical", clamp(card.getPhysical() + core, card.getPhysical()));

        Map<String, Double> weights = POSITION_WEIGHTS.get(position);

        weights.forEach((stat, weight) -> {
            double change = delta * 0.8 * weight;
            change = clamp(change, -0.6, 0.6);
            Double oldValue = getStat(card, stat);
            if (oldValue < 50 && change > 0) {
                change *= 0.5;
            }
            double newValue = clamp(oldValue + change, oldValue);
            setStat(card, stat, newValue);
        });

        // overall
        card.setOverallRating(calculateOverall(card, position));
        card.setLastUpdated(Instant.now());

        cardRepo.save(card);

        double newRating = card.getOverallRating();

        historyRepo.save(snapshot(card, EventType.TOURNAMENT_MATCH, match.getId()));

        if (Math.round(oldRating) != Math.round(newRating)) {
            notificationEvents.ratingUpdatedAfterTournament(user, oldRating, newRating, match);
        }
    }


    private void applyGoalkeeperDelta(PlayerCardStats card, double delta, TournamentMatch match, User user, double oldRating) {
        double core = clamp(delta * 0.2, -0.3, 0.3);

        setStat(card, "gkDiving", clamp(card.getGkDiving() + core, card.getGkDiving()));
        setStat(card, "gkHandling", clamp(card.getGkHandling() + core, card.getGkHandling()));
        setStat(card, "gkKicking", clamp(card.getGkKicking() + core, card.getGkKicking()));
        setStat(card, "gkReflexes", clamp(card.getGkReflexes() + core, card.getGkReflexes()));
        setStat(card, "gkSpeed", clamp(card.getGkSpeed() + core, card.getGkSpeed()));
        setStat(card, "gkPositioning", clamp(card.getGkPositioning() + core, card.getGkPositioning()));

        Map<String, Double> weights = POSITION_WEIGHTS.get(Position.GOALKEEPER);

        weights.forEach((stat, weight) -> {
            double change = delta * 0.8 * weight;
            change = clamp(change, -0.6, 0.6);
            Double oldValue = getStat(card, stat);
            if (oldValue < 50 && change > 0) {
                change *= 0.5;
            }
            double newValue = clamp(oldValue + change, oldValue);
            setStat(card, stat, newValue);
        });

        card.setOverallRating(calculateOverall(card, Position.GOALKEEPER));
        card.setLastUpdated(Instant.now());

        cardRepo.save(card);
        double newRating = card.getOverallRating();
        historyRepo.save(snapshot(card, EventType.TOURNAMENT_MATCH, match.getId()));

        if (Math.round(oldRating) != Math.round(newRating)) {
            notificationEvents.ratingUpdatedAfterTournament(user, oldRating, newRating, match);
        }
    }

}




