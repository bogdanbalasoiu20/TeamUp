package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.mapper.TeamMapper;
import com.teamup.teamUp.model.dto.rating.team.TeamRatingDto;
import com.teamup.teamUp.model.dto.team.TeamFullProfileDto;
import com.teamup.teamUp.model.dto.team.TeamMemberResponseDto;
import com.teamup.teamUp.model.dto.team.TeamResponseDto;
import com.teamup.teamUp.model.dto.team.TeamStatisticsResponseDto;
import com.teamup.teamUp.model.dto.tournament.TeamTournamentHistoryDto;
import com.teamup.teamUp.model.entity.*;
import com.teamup.teamUp.model.enums.MatchStatus;
import com.teamup.teamUp.model.enums.SquadType;
import com.teamup.teamUp.model.enums.TeamRole;
import com.teamup.teamUp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TournamentMatchRepository tournamentMatchRepository;
    private final TournamentStandingRepository tournamentStandingRepository;
    private final TeamRatingService teamRatingService;

    @Transactional
    public TeamResponseDto createTeam(String name, String captainUsername) {

        if (teamRepository.existsByName(name)) {
            throw new IllegalArgumentException("Team name already exists");
        }

        User captain = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(captainUsername).orElseThrow(() -> new NotFoundException("User not found"));

        Team team = Team.builder()
                .name(name)
                .captain(captain)
                .teamChemistry(0.0)
                .build();

        teamRepository.save(team);

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(captain)
                .role(TeamRole.CAPTAIN)
                .build();

        teamMemberRepository.save(member);

        return TeamMapper.toDto(team);
    }

    @Transactional
    public void addPlayer(UUID teamId, UUID userId, String captainUsername) {

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new NotFoundException("User already in team");
        }

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new NotFoundException("Team not found"));

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        if (!team.getCaptain().getUsername().equals(captainUsername)) {
            throw new AccessDeniedException("Only captain can add players");
        }

        List<TeamMember> benchMembers = teamMemberRepository.findByTeamIdAndSquadType(teamId, SquadType.BENCH);

        Set<Integer> usedSlots = benchMembers.stream()
                .map(TeamMember::getSlotIndex)
                .collect(Collectors.toSet());

        int nextSlot = 0;
        while (usedSlots.contains(nextSlot)) {
            nextSlot++;
        }


        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .role(TeamRole.PLAYER)
                .squadType(SquadType.BENCH)
                .slotIndex(nextSlot)
                .build();

        teamMemberRepository.save(member);
    }


    @Transactional(readOnly = true)
    public List<TeamMemberResponseDto> getMembers(UUID teamId) {

        if (!teamRepository.existsById(teamId)) {
            throw new NotFoundException("Team not found");
        }

        return teamMemberRepository.findByTeamId(teamId)
                .stream()
                .sorted(Comparator
                        .comparing(TeamMember::getSquadType)
                        .thenComparing(TeamMember::getSlotIndex))
                .map(member -> new TeamMemberResponseDto(
                        member.getUser().getId(),
                        member.getUser().getUsername(),
                        member.getRole(),
                        member.getJoinedAt(),
                        member.getSquadType(),
                        member.getSlotIndex()
                ))
                .toList();
    }


    @Transactional(readOnly = true)
    public TeamResponseDto getTeam(UUID teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new NotFoundException("Team not found"));
        return TeamMapper.toDto(team);
    }


    @Transactional(readOnly = true)
    public List<TeamResponseDto> getTeamsForUser(String username) {
        return teamMemberRepository.findByUserUsernameIgnoreCase(username)
                .stream()
                .map(TeamMember::getTeam)
                .distinct()
                .map(TeamMapper::toDto)
                .toList();
    }


    @Transactional(readOnly = true)
    public Page<TeamResponseDto> exploreTeams(String username, int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Team> result = teamRepository.exploreTeams(username, search, pageable);
        return result.map(TeamMapper::toDto);
    }


    @Transactional
    public void removePlayer(UUID teamId, UUID userId, String captainUsername) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new NotFoundException("Team not found"));

        if (!team.getCaptain().getUsername().equals(captainUsername)) {
            throw new AccessDeniedException("Only captain can remove players");
        }

        TeamMember member = teamMemberRepository.findByTeamId(teamId)
                .stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Player not in team"));

        if (member.getRole() == TeamRole.CAPTAIN) {
            throw new IllegalArgumentException("Cannot remove captain");
        }

        teamMemberRepository.delete(member);
    }


    @Transactional
    public void updatePosition(UUID teamId, UUID userId, SquadType squadType, Integer slotIndex, String captainUsername){

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new NotFoundException("Team not found"));

        if(!team.getCaptain().getUsername().equals(captainUsername)) {
            throw new AccessDeniedException("Only captain can update positions");
        }

        if (slotIndex == null || slotIndex < 0) {
            throw new IllegalArgumentException("Invalid slot index");
        }

        TeamMember moving = teamMemberRepository.findByTeamIdAndUserId(teamId,userId).orElseThrow(() -> new NotFoundException("Player not in team"));

        SquadType oldType = moving.getSquadType();
        Integer oldSlotIndex = moving.getSlotIndex();

        TeamMember occupant = teamMemberRepository.findByTeamIdAndSquadTypeAndSlotIndex(teamId, squadType, slotIndex).orElse(null);

        if (occupant != null && !occupant.getUser().getId().equals(userId)) {

            // mut occupant temporar
            occupant.setSlotIndex(Integer.MIN_VALUE);
            teamMemberRepository.saveAndFlush(occupant);

            // mut moving în slotul nou
            moving.setSquadType(squadType);
            moving.setSlotIndex(slotIndex);
            teamMemberRepository.saveAndFlush(moving);

            // mut occupant în pozitia veche
            occupant.setSquadType(oldType);
            occupant.setSlotIndex(oldSlotIndex);
            teamMemberRepository.saveAndFlush(occupant);

        } else {
            moving.setSquadType(squadType);
            moving.setSlotIndex(slotIndex);
            teamMemberRepository.saveAndFlush(moving);
        }
    }



    @Transactional(readOnly = true)
    public TeamStatisticsResponseDto getTeamStatistics(UUID teamId) {

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new NotFoundException("Team not found"));

        List<TournamentMatch> matches = tournamentMatchRepository.findAllByTeamId(teamId);

        int wins = 0, draws = 0, losses = 0;
        int goalsFor = 0, goalsAgainst = 0;

        for (TournamentMatch match : matches) {
            if (match.getStatus() != MatchStatus.DONE)
                continue;

            boolean isHome = match.getHomeTeam().getId().equals(teamId);

            int scored = isHome ? match.getScoreHome() : match.getScoreAway();
            int conceded = isHome ? match.getScoreAway() : match.getScoreHome();

            goalsFor += scored;
            goalsAgainst += conceded;

            if (scored > conceded) wins++;
            else if (scored == conceded) draws++;
            else losses++;
        }

        int played = wins + draws + losses;

        int tournamentsPlayed = tournamentStandingRepository.findByTeamId(teamId).size();
        int tournamentsWon = tournamentStandingRepository.countByTeamIdAndFinalPosition(teamId, 1);

        return new TeamStatisticsResponseDto(
                teamId,
                team.getName(),
                played,
                wins,
                draws,
                losses,
                goalsFor,
                goalsAgainst,
                tournamentsPlayed,
                tournamentsWon
        );
    }


    @Transactional(readOnly = true)
    public List<TeamTournamentHistoryDto> getTeamTournamentHistory(UUID teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new NotFoundException("Team not found");
        }

        List<TournamentStanding> standings = tournamentStandingRepository.findByTeamId(teamId);

        return standings.stream()
                .map(s -> new TeamTournamentHistoryDto(
                        s.getTournament().getId(),
                        s.getTournament().getName(),
                        s.getFinalPosition() == null ? 0 : s.getFinalPosition(),
                        s.getPlayed(),
                        s.getWins(),
                        s.getDraws(),
                        s.getLosses(),
                        s.getGoalsFor(),
                        s.getGoalsAgainst()
                ))
                .toList();
    }


    @Transactional(readOnly = true)
    public TeamFullProfileDto getTeamFullProfile(UUID teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new NotFoundException("Team not found"));

        TeamResponseDto teamDto = TeamMapper.toDto(team);

        TeamRatingDto rating = teamRatingService.calculateTeamRating(teamId);

        TeamStatisticsResponseDto statistics = getTeamStatistics(teamId);
        List<TeamTournamentHistoryDto> history = getTeamTournamentHistory(teamId);

        return new TeamFullProfileDto(
                teamDto,
                statistics,
                history,
                rating
        );
    }


}

