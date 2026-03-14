package com.teamup.teamUp.service;

import com.teamup.teamUp.model.dto.rating.team.TeamRatingDto;
import com.teamup.teamUp.model.entity.Team;
import com.teamup.teamUp.model.entity.TeamMember;
import com.teamup.teamUp.model.enums.Compartment;
import com.teamup.teamUp.model.enums.Position;
import com.teamup.teamUp.model.enums.SquadType;
import com.teamup.teamUp.repository.PlayerCardStatsRepository;
import com.teamup.teamUp.repository.TeamMemberRepository;
import com.teamup.teamUp.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamRatingService {

    private final TeamMemberRepository teamMemberRepository;
    private final PlayerCardStatsRepository playerCardStatsRepository;
    private final TeamRepository teamRepository;

    public TeamRatingDto calculateTeamRating(UUID teamId){

        List<TeamMember> starters = teamMemberRepository.findStarters(teamId, SquadType.PITCH);

        if(starters.isEmpty()){
            return new TeamRatingDto(0,0,0,0);
        }

        double attSum = 0, midSum = 0, defSum = 0;
        int attCount = 0, midCount = 0, defCount = 0;

        boolean hasGoalkeeper = false;

        List<UUID> userIds = starters.stream().map(u -> u.getUser().getId()).toList();
        List<Object[]> rows = playerCardStatsRepository.findOverallForUsers(userIds);

        Map<UUID, Double> overallForUsers = rows.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Double) row[1]
                ));

        for(TeamMember starter : starters){

            Double overall = overallForUsers.getOrDefault(starter.getUser().getId(), 60.0);

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
                        hasGoalkeeper = true;
                    } else {
                        defSum += adjustedOverall * 0.7;
                    }

                    defCount++;
                }
            }
        }

        int attack = attCount == 0 ? 0 : (int) Math.round(attSum / attCount);
        int midfield = midCount == 0 ? 0 : (int) Math.round(midSum / midCount);
        int defense = defCount == 0 ? 0 : (int) Math.round(defSum / defCount);

        int totalPlayers = starters.size();

        defense = applyDefensePenalty(defense, defCount, totalPlayers);
        midfield = applyMidfieldPenalty(midfield, midCount, totalPlayers);

        if(!hasGoalkeeper){
            defense = (int)Math.round(defense * 0.75);
        }

        double overallRaw =
                attack * 0.33 +
                        midfield * 0.34 +
                        defense * 0.33;

        boolean missingLine =
                attCount == 0 ||
                        midCount == 0 ||
                        defCount == 0;

        if (missingLine) {
            overallRaw *= 0.85;
        }

        int overall = (int) Math.round(overallRaw);

        return new TeamRatingDto(attack, midfield, defense, overall);
    }

    private Compartment getCompartmentBySlot(int slotIndex){

        if(slotIndex == 0){
            return Compartment.GK;
        }

        if(slotIndex >= 1 && slotIndex <= 5){
            return Compartment.DEFENSE;
        }

        if(slotIndex >= 6 && slotIndex <= 11){
            return Compartment.MIDFIELD;
        }

        if(slotIndex >= 12 && slotIndex <= 15){
            return Compartment.ATTACK;
        }

        throw new IllegalArgumentException("Invalid slot index");
    }

    private double applyPositionPenalty(TeamMember member, double overall, Compartment current){

        if (member.getUser().getPosition() == null)
            return overall;

        Compartment natural = mapPositionToCompartment(member.getUser().getPosition());

        int distance = Math.abs(compartmentIndex(natural) - compartmentIndex(current));

        double factor = switch (distance) {
            case 0 -> 1.0;
            case 1 -> 0.90;
            case 2 -> 0.75;
            default -> 0.55;
        };

        return overall * factor;
    }

    private int compartmentIndex(Compartment c){
        return switch (c) {
            case GK -> 0;
            case DEFENSE -> 1;
            case MIDFIELD -> 2;
            case ATTACK -> 3;
        };
    }

    private Compartment mapPositionToCompartment(Position position){
        return switch (position) {
            case FORWARD -> Compartment.ATTACK;
            case MIDFIELDER -> Compartment.MIDFIELD;
            case DEFENDER -> Compartment.DEFENSE;
            case GOALKEEPER -> Compartment.GK;
        };
    }

    @Transactional
    public void recalcTeamRating(UUID teamId){

        TeamRatingDto rating = calculateTeamRating(teamId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        team.setAttackRating(rating.attack());
        team.setMidfieldRating(rating.midfield());
        team.setDefenseRating(rating.defense());
        team.setOverallRating(rating.overall());

        teamRepository.save(team);
    }

    private int applyDefensePenalty(int defenseRating, int defenders, int totalPlayers){

        double ratio = (double) defenders / totalPlayers;

        if(ratio >= 0.30)
            return defenseRating;

        if(ratio >= 0.20)
            return (int)Math.round(defenseRating * 0.9);

        return (int)Math.round(defenseRating * 0.70);
    }

    private int applyMidfieldPenalty(int midfieldRating, int mids, int totalPlayers){

        double ratio = (double) mids / totalPlayers;

        if(ratio >= 0.25)
            return midfieldRating;

        if(ratio >= 0.15)
            return (int)Math.round(midfieldRating * 0.90);

        return (int)Math.round(midfieldRating * 0.75);
    }
}