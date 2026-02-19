package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.entity.PlayerCardStats;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final PlayerCardStatsRepository playerCardStatsRepository;

    @Transactional
    public Team createTeam(String name, String captainUsername) {

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

        return team;
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
}

