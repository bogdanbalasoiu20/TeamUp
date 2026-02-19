package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.dto.tournament.CreateTournamentRequestDto;
import com.teamup.teamUp.model.entity.*;
import com.teamup.teamUp.model.enums.MatchStatus;
import com.teamup.teamUp.model.enums.TournamentStatus;
import com.teamup.teamUp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentTeamRepository tournamentTeamRepository;
    private final TournamentStandingRepository standingRepository;
    private final TournamentMatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Transactional
    public Tournament createTournament(CreateTournamentRequestDto request, String organizerUsername) {

        User organizer = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(organizerUsername).orElseThrow(() -> new NotFoundException("Organizer not found"));

        if (request.getMaxTeams() == null || request.getMaxTeams() < 2) {
            throw new IllegalArgumentException("Tournament must allow at least 2 teams");
        }

        if (request.getStartsAt() == null || request.getEndsAt() == null) {
            throw new IllegalArgumentException("Tournament period must be defined");
        }

        if (!request.getEndsAt().isAfter(request.getStartsAt())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        Tournament tournament = Tournament.builder()
                .name(request.getName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .maxTeams(request.getMaxTeams())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .organizer(organizer)
                .status(TournamentStatus.OPEN)
                .build();

        return tournamentRepository.save(tournament);
    }


    @Transactional
    public void joinTournament(UUID tournamentId, UUID teamId) {

        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(() -> new NotFoundException("Tournament not found"));

        if (tournament.getStatus() != TournamentStatus.OPEN) {
            throw new RuntimeException("Tournament already started");
        }

        if (tournamentTeamRepository.existsByTournamentIdAndTeamId(tournamentId, teamId)) {
            throw new RuntimeException("Team already joined");
        }

        long count = tournamentTeamRepository.findByTournamentId(tournamentId).size();

        if (count >= tournament.getMaxTeams()) {
            throw new RuntimeException("Tournament is full");
        }

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new NotFoundException("Team not found"));

        TournamentTeam tt = TournamentTeam.builder()
                .tournament(tournament)
                .team(team)
                .build();

        tournamentTeamRepository.save(tt);
    }

    @Transactional
    public void startTournament(UUID tournamentId, String authUsername) {

        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(() -> new NotFoundException("Tournament not found"));

        if (!tournament.getOrganizer().getUsername().equals(authUsername)) {
            throw new AccessDeniedException("Only organizer can start tournament");
        }


        if (tournament.getStatus() != TournamentStatus.OPEN) {
            throw new RuntimeException("Tournament already started");
        }

        var teams = tournamentTeamRepository.findByTournamentId(tournamentId);

        if (teams.size() < 2) {
            throw new RuntimeException("Not enough teams");
        }

        tournament.setStatus(TournamentStatus.STARTED);

        // create standings
        for (TournamentTeam tt : teams) {
            TournamentStanding standing = TournamentStanding.builder()
                    .tournament(tournament)
                    .team(tt.getTeam())
                    .build();

            standingRepository.save(standing);
        }

        generateRoundRobinMatches(tournament, teams);
    }

    private void generateRoundRobinMatches(Tournament tournament, List<TournamentTeam> teams) {
        int matchDay = 1;
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {

                TournamentMatch match = TournamentMatch.builder()
                        .tournament(tournament)
                        .homeTeam(teams.get(i).getTeam())
                        .awayTeam(teams.get(j).getTeam())
                        .matchDay(matchDay++)
                        .build();

                matchRepository.save(match);
            }
        }
    }

    @Transactional
    public void finishTournamentIfCompleted(UUID tournamentId) {

        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(() -> new NotFoundException("Tournament not found"));

        var openMatches = matchRepository.findByTournamentIdAndStatus(tournamentId, MatchStatus.OPEN);

        if (openMatches.isEmpty()) {
            tournament.setStatus(TournamentStatus.FINISHED);
        }
    }
}

