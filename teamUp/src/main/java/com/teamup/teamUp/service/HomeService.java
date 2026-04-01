package com.teamup.teamUp.service;

import com.teamup.teamUp.model.dto.dashboard.*;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.entity.PlayerCardStatsHistory;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.MatchParticipantStatus;
import com.teamup.teamUp.repository.MatchParticipantRepository;
import com.teamup.teamUp.repository.PlayerCardStatsHistoryRepository;
import com.teamup.teamUp.repository.PlayerCardStatsRepository;
import com.teamup.teamUp.repository.TournamentMatchParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final MatchService matchService;
    private final TournamentService tournamentService;
    private final MatchParticipantRepository matchParticipantRepository;
    private final TournamentMatchParticipantRepository tournamentMatchParticipantRepository;
    private final UserService userService;
    private final PlayerCardStatsRepository playerCardStatsRepository;
    private final PlayerCardStatsHistoryRepository playerCardStatsHistoryRepository;

    public HomeResponse getHome(String username) {

        HomeUpcomingResponse upcoming = getUpcomingForCurrentUser(username);
        MonthlyStatsDto stats = getMonthlyStats(username);
        UserHomeStatsDto userStats = getUserStats(username);

        return new HomeResponse(upcoming, stats, userStats);
    }


    public HomeUpcomingResponse getUpcomingForCurrentUser(String username) {

        List<UpcomingMatchDto> matches = matchService.getUpcomingMatchesForUser(username);

        List<UpcomingTournamentDto> tournaments = tournamentService.getUpcomingTournamentsForUser(username);

        return new HomeUpcomingResponse(matches, tournaments);
    }


    public MonthlyStatsDto getMonthlyStats(String username) {

        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        ZoneId zone = ZoneId.systemDefault();

        Instant startThisMonth = currentMonth.atDay(1).atStartOfDay(zone).toInstant();
        Instant endThisMonth = currentMonth.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant();

        Instant startLastMonth = previousMonth.atDay(1).atStartOfDay(zone).toInstant();
        Instant endLastMonth = currentMonth.atDay(1).atStartOfDay(zone).toInstant();

        // OPEN MATCHES
        long openThisMonth = matchParticipantRepository.countMatchesBetween(username, MatchParticipantStatus.ACCEPTED, startThisMonth, endThisMonth);

        long openLastMonth = matchParticipantRepository.countMatchesBetween(username, MatchParticipantStatus.ACCEPTED, startLastMonth, endLastMonth);

        // TOURNAMENTS
        LocalDateTime startThisMonthLdt = LocalDateTime.ofInstant(startThisMonth, zone);
        LocalDateTime endThisMonthLdt = LocalDateTime.ofInstant(endThisMonth, zone);

        LocalDateTime startLastMonthLdt = LocalDateTime.ofInstant(startLastMonth, zone);
        LocalDateTime endLastMonthLdt = LocalDateTime.ofInstant(endLastMonth, zone);

        long tournamentsThisMonth = tournamentMatchParticipantRepository.countTournamentsBetween(username, startThisMonthLdt, endThisMonthLdt);

        long tournamentsLastMonth = tournamentMatchParticipantRepository.countTournamentsBetween(username, startLastMonthLdt, endLastMonthLdt);

        // TOTAL (match-uri + turnee)
        long totalThisMonth = openThisMonth + tournamentsThisMonth;
        long totalLastMonth = openLastMonth + tournamentsLastMonth;

        double percentage = calculatePercentage(totalThisMonth, totalLastMonth);

        return new MonthlyStatsDto(
                totalThisMonth,
                totalLastMonth,
                percentage,
                openThisMonth,
                tournamentsThisMonth
        );
    }

    private double calculatePercentage(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((current - previous) * 100.0) / previous;
    }

    private UserHomeStatsDto getUserStats(String username) {

        User user = userService.findByUsername(username);

        UUID userId = user.getId();

        PlayerCardStats currentStats =
                playerCardStatsRepository.findById(userId).orElseThrow();

        double current = currentStats.getOverallRating();

        Page<PlayerCardStatsHistory> page =
                playerCardStatsHistoryRepository.findLatestStats(userId, PageRequest.of(0, 2));

        List<PlayerCardStatsHistory> history = page.getContent();

        double previous = current;

        if (history.size() >= 2) {
            previous = history.get(1).getOverallRating();
        }

        int change = (int) Math.round(current - previous);

        return new UserHomeStatsDto(
                (int) Math.round(current),
                user.getPosition(),
                user.getPhotoUrl(),
                change
        );
    }
}
