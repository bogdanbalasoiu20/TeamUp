package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.dto.TeamChemistryDto;
import com.teamup.teamUp.model.entity.TeamMember;
import com.teamup.teamUp.model.enums.Compartment;
import com.teamup.teamUp.model.enums.SquadType;
import com.teamup.teamUp.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TeamChemistryService {

    private final TeamMemberRepository teamMemberRepository;
    private final ChemistryService chemistryService;

    private static record PlayerPair(UUID a, UUID b) {

        static PlayerPair of(UUID u1, UUID u2) {
            return u1.compareTo(u2) < 0 ? new PlayerPair(u1, u2) : new PlayerPair(u2, u1);
        }
    }

    public TeamChemistryDto calculateTeamChemistry(UUID teamId){

        List<TeamMember> starters = teamMemberRepository.findByTeamIdAndSquadType(teamId, SquadType.PITCH);

        if(starters.isEmpty())
            return new TeamChemistryDto(0,0,0,0,0,0,0);

        Map<PlayerPair,Integer> pairCache = new HashMap<>();

        List<TeamMember> gk = new ArrayList<>();
        List<TeamMember> def = new ArrayList<>();
        List<TeamMember> mid = new ArrayList<>();
        List<TeamMember> att = new ArrayList<>();

        for(TeamMember m : starters){
            Compartment comp = getCompartmentBySlot(m.getSlotIndex());

            switch (comp){
                case GK -> gk.add(m);
                case DEFENSE -> def.add(m);
                case MIDFIELD -> mid.add(m);
                case ATTACK -> att.add(m);
            }
        }

        int gkDefChem = avgBetween(gk, def, pairCache);
        int defChem = avgWithin(def, pairCache);
        int midChem = avgWithin(mid, pairCache);
        int attChem = avgWithin(att, pairCache);

        int defMidChem = avgBetween(def, mid, pairCache);
        int midAttChem = avgBetween(mid, att, pairCache);

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

    private int avgWithin(List<TeamMember> group, Map<PlayerPair,Integer> cache){

        if(group.size() < 2)
            return 0;

        int sum = 0;
        int pairs = 0;

        for(int i = 0; i < group.size(); i++){
            UUID a = group.get(i).getUser().getId();
            for(int j = i + 1; j < group.size(); j++){
                UUID b = group.get(j).getUser().getId();
                int chem = getChemistry(a,b,cache);
                sum += chem;
                pairs++;
            }
        }

        return pairs == 0 ? 0 : sum / pairs;
    }

    private int avgBetween(List<TeamMember> a, List<TeamMember> b, Map<PlayerPair,Integer> cache){

        if(a.isEmpty() || b.isEmpty())
            return 0;

        int sum = 0;
        int pairs = 0;

        for(TeamMember m1 : a){
            UUID userA = m1.getUser().getId();
            for(TeamMember m2 : b){
                UUID userB = m2.getUser().getId();
                int chem = getChemistry(userA,userB,cache);
                sum += chem;
                pairs++;
            }
        }

        return pairs == 0 ? 0 : sum / pairs;
    }

    private int getChemistry(UUID a, UUID b, Map<PlayerPair,Integer> cache){

        PlayerPair key = PlayerPair.of(a,b);

        Integer value = cache.get(key);

        if(value != null)
            return value;

        int chem = chemistryService.compute(a,b).score();

        cache.put(key,chem);

        return chem;
    }



    private Compartment getCompartmentBySlot(int slotIndex){

        if(slotIndex == 0)
            return Compartment.GK;

        if(slotIndex >= 1 && slotIndex <= 5)
            return Compartment.DEFENSE;

        if(slotIndex >= 6 && slotIndex <= 11)
            return Compartment.MIDFIELD;

        if(slotIndex >= 12 && slotIndex <= 15)
            return Compartment.ATTACK;

        throw new IllegalArgumentException("Invalid slot index");
    }


}


