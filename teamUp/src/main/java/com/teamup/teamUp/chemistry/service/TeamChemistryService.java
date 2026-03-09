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
            return u1.compareTo(u2) < 0
                    ? new PlayerPair(u1, u2)
                    : new PlayerPair(u2, u1);
        }
    }

    enum PositionGroup {
        GK,
        DEF,
        MID,
        ATT
    }

    private static final List<PitchPosition> PITCH_POSITIONS = List.of(
            new PitchPosition(12,-0.8,-0.85),
            new PitchPosition(13,-0.25,-0.95),
            new PitchPosition(14,0.25,-0.95),
            new PitchPosition(15,0.8,-0.85),

            new PitchPosition(6,-0.90,-0.30),
            new PitchPosition(7,-0.40,-0.10),
            new PitchPosition(8,0.0,0.1),
            new PitchPosition(9,0.0,-0.5),
            new PitchPosition(10,0.40,-0.10),
            new PitchPosition(11,0.90,-0.30),

            new PitchPosition(1,-0.90,0.40),
            new PitchPosition(2,-0.45,0.50),
            new PitchPosition(3,0.0,0.55),
            new PitchPosition(4,0.45,0.50),
            new PitchPosition(5,0.90,0.40),

            new PitchPosition(0,0.0,1.0)
    );

    private static final Map<Integer, PitchPosition> POSITION_MAP =
            PITCH_POSITIONS.stream()
                    .collect(Collectors.toMap(PitchPosition::slotIndex, p -> p));

    private static final Map<Integer, PositionGroup> SLOT_GROUP = Map.ofEntries(

            Map.entry(0,PositionGroup.GK),

            Map.entry(1,PositionGroup.DEF),
            Map.entry(2,PositionGroup.DEF),
            Map.entry(3,PositionGroup.DEF),
            Map.entry(4,PositionGroup.DEF),
            Map.entry(5,PositionGroup.DEF),

            Map.entry(7,PositionGroup.MID),
            Map.entry(8,PositionGroup.MID),
            Map.entry(10,PositionGroup.MID),

            Map.entry(6,PositionGroup.MID),
            Map.entry(9,PositionGroup.MID),
            Map.entry(11,PositionGroup.MID),

            Map.entry(12,PositionGroup.ATT),
            Map.entry(13,PositionGroup.ATT),
            Map.entry(14,PositionGroup.ATT),
            Map.entry(15,PositionGroup.ATT)
    );

    public TeamChemistryResponseDto calculateTeamChemistry(UUID teamId){

        List<TeamMember> starters =
                teamMemberRepository.findByTeamIdAndSquadType(teamId,SquadType.PITCH);

        if(starters.isEmpty())
            return new TeamChemistryResponseDto(0,List.of());

        Map<Integer,UUID> slotToUser=new HashMap<>();

        for(TeamMember m:starters)
            slotToUser.put(m.getSlotIndex(),m.getUser().getId());

        Set<PlayerPair> pairs=generateLinks(slotToUser);

        List<TeamChemistryLinkDto> links=new ArrayList<>();
        Map<PlayerPair,Integer> cache=new HashMap<>();

        double sum=0;

        for(PlayerPair pair:pairs){

            int chemistry=cache.computeIfAbsent(
                    pair,
                    p->chemistryService.compute(pair.a(),pair.b()).score()
            );

            links.add(new TeamChemistryLinkDto(pair.a(),pair.b(),chemistry));

            sum+=chemistry;
        }

        int overall=pairs.isEmpty()?0:(int)Math.round(sum/pairs.size());

        return new TeamChemistryResponseDto(overall,links);
    }

    private Set<PlayerPair> generateLinks(Map<Integer,UUID> slotToUser){

        Map<PositionGroup,List<Integer>> groups=new HashMap<>();

        for(Integer slot:slotToUser.keySet()){

            PositionGroup g=SLOT_GROUP.get(slot);

            if(g==null)
                continue;

            groups.computeIfAbsent(g,k->new ArrayList<>()).add(slot);
        }

        groups.values().forEach(list ->
                list.sort(Comparator.comparingDouble(s->POSITION_MAP.get(s).x()))
        );

        Set<PlayerPair> pairs=new HashSet<>();

        // linkuri in acelasi compartiment (vecini stanga-dreapta)
        for(List<Integer> slots:groups.values()){

            for(int i=0;i<slots.size()-1;i++){

                UUID a=slotToUser.get(slots.get(i));
                UUID b=slotToUser.get(slots.get(i+1));

                pairs.add(PlayerPair.of(a,b));
            }
        }

        // legaturi intre compartimente
        connectAdjacent(groups,slotToUser,PositionGroup.GK,PositionGroup.DEF,pairs);
        connectAdjacent(groups,slotToUser,PositionGroup.DEF,PositionGroup.MID,pairs);
        connectAdjacent(groups,slotToUser,PositionGroup.MID,PositionGroup.ATT,pairs);

        return pairs;
    }

    private void connectAdjacent(
            Map<PositionGroup,List<Integer>> groups,
            Map<Integer,UUID> slotToUser,
            PositionGroup g1,
            PositionGroup g2,
            Set<PlayerPair> pairs
    ){

        List<Integer> a=groups.getOrDefault(g1,List.of());
        List<Integer> b=groups.getOrDefault(g2,List.of());

        if(a.isEmpty()||b.isEmpty())
            return;

        for(Integer s1:a){

            PitchPosition p1=POSITION_MAP.get(s1);

            b.stream()
                    .sorted(Comparator.comparingDouble(s->
                            Math.abs(p1.x()-POSITION_MAP.get(s).x())))
                    .limit(2)
                    .forEach(s2->pairs.add(
                            PlayerPair.of(
                                    slotToUser.get(s1),
                                    slotToUser.get(s2)
                            )
                    ));
        }
    }
}