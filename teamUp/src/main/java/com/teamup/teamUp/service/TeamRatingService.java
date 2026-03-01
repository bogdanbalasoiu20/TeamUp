package com.teamup.teamUp.service;

import com.teamup.teamUp.model.dto.rating.team.TeamRatingDto;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.entity.TeamMember;
import com.teamup.teamUp.model.enums.Compartment;
import com.teamup.teamUp.model.enums.Position;
import com.teamup.teamUp.model.enums.SquadType;
import com.teamup.teamUp.repository.PlayerCardStatsRepository;
import com.teamup.teamUp.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamRatingService {
    private final TeamMemberRepository teamMemberRepository;
    private final PlayerCardStatsRepository playerCardStatsRepository;

    public TeamRatingDto calculateTeamRating(UUID teamId){
        List<TeamMember> starters = teamMemberRepository.findByTeamIdAndSquadType(teamId, SquadType.PITCH);

        if(starters.isEmpty()){
            return new TeamRatingDto(0,0,0,0);
        }

        double attSum = 0, midSum = 0, defSum = 0;
        int attCount = 0, midCount = 0, defCount = 0;

        List<UUID> userIds = starters.stream().map(u-> u.getUser().getId()).toList();
        List<Object[]> rows = playerCardStatsRepository.findOverallForUsers(userIds);

        Map<UUID, Double> overallForUsers = rows.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Double) row[1]
                ));
        for(TeamMember starter : starters){
            Double overall = overallForUsers.get(starter.getUser().getId());

            Compartment compartment = getCompartmentBySlot(starter.getSlotIndex());
            double adjustedOverall = applyPositionPenalty(starter, overall, compartment);

            switch(compartment){
                case ATTACK -> {
                    attSum += adjustedOverall;
                    attCount++;
                }
                case MIDFIELD -> {
                    midSum += adjustedOverall;
                    midCount++;
                }
                case DEFENSE -> {
                    defSum += adjustedOverall;
                    defCount++;
                }
                case GK -> {
                    if (starter.getUser().getPosition() == Position.GOALKEEPER) {
                        defSum += adjustedOverall * 1.1;
                    } else {
                        defSum += adjustedOverall * 0.7;
                    }
                    defCount++;
                }
            }
        }

        int attack = attCount == 0?0:(int) Math.round(attSum/attCount);
        int midfield = midCount == 0?0:(int) Math.round(midSum/midCount);
        int defense = defCount == 0?0:(int) Math.round(defSum/defCount);

        double overallRaw = attack * 0.35 + midfield * 0.35 + defense * 0.30;

        boolean missingLine = attCount == 0 || midCount == 0 || defCount == 0;

        if (missingLine) {
            overallRaw *= 0.85;
        }

        int overall = (int) Math.round(overallRaw);

        return new TeamRatingDto(attack, midfield,defense,overall);
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

    private double applyPositionPenalty(TeamMember member, double overall, Compartment current) {
        if (member.getUser().getPosition() == null)
            return overall;

        Compartment natural = mapPositionToCompartment(member.getUser().getPosition());

        if (natural == current)
            return overall;

        if (areRelated(natural, current))
            return overall * 0.9;

        return overall * 0.8;
    }


    private Compartment mapPositionToCompartment(Position position) {

        return switch (position) {
            case FORWARD -> Compartment.ATTACK;
            case MIDFIELDER -> Compartment.MIDFIELD;
            case DEFENDER -> Compartment.DEFENSE;
            case GOALKEEPER -> Compartment.GK;
        };
    }

    private boolean areRelated(Compartment a, Compartment b) {

        if ((a == Compartment.MIDFIELD && b == Compartment.ATTACK) ||
                (a == Compartment.ATTACK && b == Compartment.MIDFIELD))
            return true;

        if ((a == Compartment.MIDFIELD && b == Compartment.DEFENSE) ||
                (a == Compartment.DEFENSE && b == Compartment.MIDFIELD))
            return true;

        return false;
    }
}

