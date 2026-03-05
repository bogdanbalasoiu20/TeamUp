package com.teamup.teamUp.chemistry;

import com.teamup.teamUp.chemistry.dto.TeamChemistryDto;
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

    @Async
    @Transactional
    public void recalcTeamChemistry(UUID teamId){

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new NotFoundException("Team not found"));

        TeamChemistryDto dto = teamChemistryService.calculateTeamChemistry(teamId);

        team.setTeamChemistry(dto.overallChem());
    }
}