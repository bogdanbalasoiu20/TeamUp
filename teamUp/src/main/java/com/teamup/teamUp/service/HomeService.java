package com.teamup.teamUp.service;

import com.teamup.teamUp.model.dto.dashboard.*;
import com.teamup.teamUp.model.enums.MatchParticipantStatus;
import com.teamup.teamUp.repository.MatchParticipantRepository;
import com.teamup.teamUp.repository.TournamentMatchParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final MatchService matchService;
    private final TournamentService tournamentService;
    private final MatchParticipantRepository matchParticipantRepository;
    private final TournamentMatchParticipantRepository tournamentMatchParticipantRepository;

    public HomeResponse getHome(String username) {

        HomeUpcomingResponse upcoming = getUpcomingForCurrentUser(username);
        MonthlyStatsDto stats = getMonthlyStats(username);

        return new HomeResponse(upcoming, stats);
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
}
