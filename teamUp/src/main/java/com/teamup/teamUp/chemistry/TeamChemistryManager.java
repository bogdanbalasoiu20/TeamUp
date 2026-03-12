package com.teamup.teamUp.chemistry;

import com.teamup.teamUp.chemistry.dto.TeamChemistryResponseDto;
import com.teamup.teamUp.chemistry.service.TeamChemistryService;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.entity.Team;
import com.teamup.teamUp.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamChemistryManager {

    private final TeamRepository teamRepository;
    private final TeamChemistryService teamChemistryService;

    @Transactional
    public void recalcTeamChemistry(UUID teamId){

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new NotFoundException("Team not found"));

        TeamChemistryResponseDto chemistry = teamChemistryService.calculateTeamChemistry(teamId);

        team.setTeamChemistry(chemistry.teamChemistry());
        teamRepository.saveAndFlush(team);
    }
}