package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.dto.TeamChemistryDto;
import com.teamup.teamUp.model.entity.PlayerChemistry;
import com.teamup.teamUp.model.entity.TeamMember;
import com.teamup.teamUp.model.enums.Compartment;
import com.teamup.teamUp.model.enums.SquadType;
import com.teamup.teamUp.repository.PlayerChemistryRepository;
import com.teamup.teamUp.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamChemistryService {

    private final TeamMemberRepository teamMemberRepository;
    private final PlayerChemistryCacheService chemistryCache;
    private final PlayerChemistryRepository playerChemistryRepository;

    public TeamChemistryDto calculateTeamChemistry(UUID teamId){

        List<TeamMember> starters = teamMemberRepository.findByTeamIdAndSquadType(teamId, SquadType.PITCH);

        List<UUID> userIds = starters.stream()
                .map(m -> m.getUser().getId())
                .toList();

        Map<String, Integer> chemistryMap = playerChemistryRepository.findForUsers(userIds).stream()
                .collect(Collectors.toMap(
                        pc -> buildKey(pc.getUserA(), pc.getUserB()),
                        PlayerChemistry::getChemistryScore
                ));

        if(starters.isEmpty())
            return new TeamChemistryDto(0,0,0,0,0,0,0);

        List<TeamMember> gk = new ArrayList<>();
        List<TeamMember> def = new ArrayList<>();
        List<TeamMember> mid = new ArrayList<>();
        List<TeamMember> att = new ArrayList<>();

        for(TeamMember m : starters){
            Compartment comp =getCompartmentBySlot(m.getSlotIndex());

            switch (comp){
                case GK -> gk.add(m);
                case DEFENSE -> def.add(m);
                case MIDFIELD -> mid.add(m);
                case ATTACK -> att.add(m);
            }
        }

        int gkDefChem = avgBetween(gk, def, chemistryMap);
        int defChem = avgWithin(def, chemistryMap);
        int midChem = avgWithin(mid, chemistryMap);
        int attChem = avgWithin(att, chemistryMap);

        int defMidChem = avgBetween(def, mid, chemistryMap);
        int midAttChem = avgBetween(mid, att, chemistryMap);

        double overallRaw = defChem * 0.30 +
                        midChem * 0.30 +
                        attChem * 0.20 +
                        defMidChem * 0.10 +
                        midAttChem * 0.07 +
                        gkDefChem * 0.03;

        int overall = (int)Math.round(overallRaw);

        return new TeamChemistryDto(
                defChem,
                midChem,
                attChem,
                gkDefChem,
                defMidChem,
                midAttChem,
                overall
        );
    }

    private int avgWithin(List<TeamMember> group, Map<String,Integer> chemistryMap){

        if(group.size() < 2)
            return 0;

        int sum = 0;
        int pairs = 0;

        for(int i = 0; i < group.size(); i++){
            for(int j = i+1; j < group.size(); j++){
                int chem = getChemistryFromMap(
                        group.get(i).getUser().getId(),
                        group.get(j).getUser().getId(),
                        chemistryMap
                );
                sum += chem;
                pairs++;
            }
        }

        return pairs == 0 ? 0 : sum / pairs;
    }

    private int avgBetween(List<TeamMember> a, List<TeamMember> b, Map<String,Integer> chemistryMap){
        if(a.isEmpty() || b.isEmpty())
            return 0;

        int sum = 0;
        int pairs = 0;

        for(TeamMember m1 : a){
            for(TeamMember m2 : b){
                int chem = getChemistryFromMap(
                        m1.getUser().getId(),
                        m2.getUser().getId(),
                        chemistryMap
                );
                sum += chem;
                pairs++;
            }
        }

        return pairs == 0 ? 0 : sum / pairs;
    }


    private Compartment getCompartmentBySlot(int slotIndex){
        if(slotIndex == 0){
            return Compartment.GK;
        }
        if(slotIndex>=1 && slotIndex<=5){
            return Compartment.DEFENSE;
        }
        if(slotIndex>=6 && slotIndex<=11){
            return Compartment.MIDFIELD;
        }
        if(slotIndex>=12 && slotIndex<=15){
            return Compartment.ATTACK;
        }

        throw new IllegalArgumentException("Invalid slot index");
    }


    private String buildKey(UUID a, UUID b) {
        return a.compareTo(b) < 0 ? a + "-" + b : b + "-" + a;
    }

    private int getChemistryFromMap(UUID a, UUID b, Map<String,Integer> chemistryMap){
        String key = buildKey(a,b);
        Integer value = chemistryMap.get(key);

        if(value != null)
            return value;

        return chemistryCache.getChemistry(a,b);
    }
}
