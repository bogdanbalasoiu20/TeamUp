package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.mapper.StandingMapper;
import com.teamup.teamUp.mapper.TournamentMapper;
import com.teamup.teamUp.mapper.TournamentMatchMapper;
import com.teamup.teamUp.model.dto.tournament.CreateTournamentRequestDto;
import com.teamup.teamUp.model.dto.tournament.TournamentMatchResponseDto;
import com.teamup.teamUp.model.dto.tournament.TournamentResponseDto;
import com.teamup.teamUp.model.dto.tournament.TournamentStandingResponseDto;
import com.teamup.teamUp.model.entity.*;
import com.teamup.teamUp.model.enums.MatchStatus;
import com.teamup.teamUp.model.enums.TournamentStatus;
import com.teamup.teamUp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final VenueRepository venueRepository;

    @Transactional
    public TournamentResponseDto createTournament(CreateTournamentRequestDto request, String organizerUsername) {

        User organizer = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(organizerUsername).orElseThrow(() -> new NotFoundException("Organizer not found"));
        Venue venue = venueRepository.findById(request.getVenueId()).orElseThrow(() -> new NotFoundException("Venue not found"));


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
                .venue(venue)
                .maxTeams(request.getMaxTeams())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .organizer(organizer)
                .status(TournamentStatus.OPEN)
                .build();

        tournamentRepository.save(tournament);

        return TournamentMapper.toDto(tournament);
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

        tournament.setStatus(TournamentStatus.ONGOING);

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

    private void generateRoundRobinMatches(Tournament tournament, List<TournamentTeam> tournamentTeams) {
        List<Team> teams = tournamentTeams.stream()
                .map(TournamentTeam::getTeam)
                .toList();

        int n = teams.size();

        // Daca e numar impar, adaugam null (BYE)
        if (n % 2 != 0) {
            teams = new ArrayList<>(teams);
            teams.add(null);
            n++;
        }

        int totalRounds = n - 1;
        int matchesPerRound = n / 2;

        List<Team> rotation = new ArrayList<>(teams);

        for (int round = 0; round < totalRounds; round++) {
            for (int i = 0; i < matchesPerRound; i++) {
                Team home = rotation.get(i);
                Team away = rotation.get(n - 1 - i);

                if (home != null && away != null) {
                    TournamentMatch match = TournamentMatch.builder()
                            .tournament(tournament)
                            .homeTeam(home)
                            .awayTeam(away)
                            .matchDay(round + 1)
                            .build();

                    matchRepository.save(match);
                }
            }

            // rotatie (pastram prima echipa fixa)
            Team last = rotation.remove(rotation.size() - 1);
            rotation.add(1, last);
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

    @Transactional(readOnly = true)
    public TournamentResponseDto getTournament(UUID tournamentId) {
        Tournament tournament =  tournamentRepository.findById(tournamentId).orElseThrow(() -> new NotFoundException("Tournament not found"));
        return TournamentMapper.toDto(tournament);
    }

    @Transactional(readOnly = true)
    public List<TournamentMatchResponseDto> getMatches(UUID tournamentId) {

        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(() -> new NotFoundException("Tournament not found"));

        List<TournamentMatch> matches = matchRepository.findByTournamentId(tournament.getId());

        return matches.stream().map(TournamentMatchMapper::toDto).toList();
    }


    @Transactional(readOnly = true)
    public List<TournamentStandingResponseDto> getStandings(UUID tournamentId) {

        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(() -> new NotFoundException("Tournament not found"));

        // Daca turneul nu a inceput inca → clasament virtual
        if (tournament.getStatus() == TournamentStatus.OPEN) {
            var teams = tournamentTeamRepository.findByTournamentId(tournamentId);

            return teams.stream()
                    .map(tt -> new TournamentStandingResponseDto(
                            tt.getTeam().getId(),
                            tt.getTeam().getName(),
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0
                    ))
                    .toList();
        }

        // Daca turneul a inceput → standings reale
        return standingRepository.findByTournamentIdOrderByPointsDescGoalsForDesc(tournamentId)
                .stream()
                .map(StandingMapper::toDto)
                .toList();
    }


    @Transactional(readOnly = true)
    public Page<TournamentResponseDto> getTournaments(TournamentStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startsAt").descending());

        Page<Tournament> result;
        if (status == null) {
            result = tournamentRepository.findAll(pageable);
        } else {
            result = tournamentRepository.findByStatus(status, pageable);
        }

        return result.map(TournamentMapper::toDto);
    }




}

