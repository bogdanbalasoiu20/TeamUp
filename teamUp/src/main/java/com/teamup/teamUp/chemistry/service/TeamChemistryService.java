package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.PitchPosition;
import com.teamup.teamUp.chemistry.dto.TeamChemistryLinkDto;
import com.teamup.teamUp.chemistry.dto.TeamChemistryResponseDto;
import com.teamup.teamUp.model.entity.TeamMember;
import com.teamup.teamUp.model.enums.SquadType;
import com.teamup.teamUp.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamChemistryService {

    private final TeamMemberRepository teamMemberRepository;
    private final ChemistryService chemistryService;

    private record PlayerPair(UUID a, UUID b) {

        static PlayerPair of(UUID u1, UUID u2) {
            return u1.compareTo(u2) < 0 ? new PlayerPair(u1, u2) : new PlayerPair(u2, u1);
        }
    }

    private static final List<PitchPosition> PITCH_POSITIONS = List.of(
            new PitchPosition(12, -0.8, -0.85),
            new PitchPosition(13, -0.25, -0.95),
            new PitchPosition(14, 0.25, -0.95),
            new PitchPosition(15, 0.8, -0.85),

            new PitchPosition(6, -0.90, -0.30),
            new PitchPosition(7, -0.40, -0.10),
            new PitchPosition(8, 0.0, 0.1),
            new PitchPosition(9, 0.0, -0.5),
            new PitchPosition(10, 0.40, -0.10),
            new PitchPosition(11, 0.90, -0.30),

            new PitchPosition(1, -0.90, 0.40),
            new PitchPosition(2, -0.45, 0.50),
            new PitchPosition(3, 0.0, 0.55),
            new PitchPosition(4, 0.45, 0.50),
            new PitchPosition(5, 0.90, 0.40),

            new PitchPosition(0, 0.0, 1.0)
    );

    private static final Map<Integer, PitchPosition> POSITION_MAP =
            PITCH_POSITIONS.stream()
                    .collect(Collectors.toMap(
                            PitchPosition::slotIndex,
                            p -> p
                    ));


    public TeamChemistryResponseDto calculateTeamChemistry(UUID teamId){
        List<TeamChemistryLinkDto> links = new ArrayList<>();

        List<TeamMember> starters = teamMemberRepository.findByTeamIdAndSquadType(teamId, SquadType.PITCH);

        if(starters.isEmpty())
            return new TeamChemistryResponseDto(0, List.of());

        Map<Integer, UUID> slotToUser = new HashMap<>();

        for(TeamMember m : starters)
            slotToUser.put(m.getSlotIndex(), m.getUser().getId());

        Map<PlayerPair,Integer> cache = new HashMap<>();

        double weightedSum = 0;
        double totalWeight = 0;

        double threshold = 0.75;

        for(var entryA : slotToUser.entrySet()){

            PitchPosition posA = POSITION_MAP.get(entryA.getKey());
            UUID userA = entryA.getValue();

            for(var entryB : slotToUser.entrySet()){

                if(entryA.getKey() >= entryB.getKey())
                    continue;

                PitchPosition posB = POSITION_MAP.get(entryB.getKey());
                UUID userB = entryB.getValue();

                double dist = distance(posA,posB);

                if(dist > threshold)
                    continue;

                PlayerPair pair = PlayerPair.of(userA,userB);

                int chem = cache.computeIfAbsent(pair, p -> chemistryService.compute(userA,userB).score());

                links.add(new TeamChemistryLinkDto(userA, userB, chem));

                double weight = 1.0 / (dist + 0.15);

                weightedSum += chem * weight;
                totalWeight += weight;
            }
        }

        if(totalWeight == 0)
            return new TeamChemistryResponseDto(0, List.of());

        int overall = (int)Math.round(weightedSum / totalWeight);

        return new TeamChemistryResponseDto(overall, links);
    }


    private double distance(PitchPosition a, PitchPosition b){

        double dx = a.x() - b.x();
        double dy = a.y() - b.y();

        return Math.sqrt(dx*dx + dy*dy);
    }


}


