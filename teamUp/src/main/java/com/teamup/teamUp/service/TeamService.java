package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.mapper.TeamMapper;
import com.teamup.teamUp.model.dto.team.TeamMemberResponseDto;
import com.teamup.teamUp.model.dto.team.TeamResponseDto;
import com.teamup.teamUp.model.entity.Team;
import com.teamup.teamUp.model.entity.TeamMember;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.TeamRole;
import com.teamup.teamUp.repository.PlayerCardStatsRepository;
import com.teamup.teamUp.repository.TeamMemberRepository;
import com.teamup.teamUp.repository.TeamRepository;
import com.teamup.teamUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final PlayerCardStatsRepository playerCardStatsRepository;

    @Transactional
    public TeamResponseDto createTeam(String name, String captainUsername) {

        if (teamRepository.existsByName(name)) {
            throw new IllegalArgumentException("Team name already exists");
        }

        User captain = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(captainUsername).orElseThrow(() -> new NotFoundException("User not found"));
        Double captainRating = playerCardStatsRepository.getOverall(captain.getId());

        Team team = Team.builder()
                .name(name)
                .captain(captain)
                .teamRating(captainRating)
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


        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .role(TeamRole.PLAYER)
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
                .map(member -> new TeamMemberResponseDto(
                        member.getUser().getId(),
                        member.getUser().getUsername(),
                        member.getRole(),
                        member.getJoinedAt()
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
}

