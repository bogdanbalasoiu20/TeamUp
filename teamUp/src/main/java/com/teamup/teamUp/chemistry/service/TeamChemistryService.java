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

    private static final List<PitchPosition> PITCH_POSITIONS = List.of(
            new PitchPosition(12, -0.8, -0.85),  // LW
            new PitchPosition(13, -0.25, -0.95), // ST
            new PitchPosition(14, 0.25, -0.95),  // ST
            new PitchPosition(15, 0.8, -0.85),   // RW

            new PitchPosition(6, -0.90, -0.30),  // LM
            new PitchPosition(7, -0.40, -0.10),  // CM
            new PitchPosition(8, 0.0, 0.1),      // CDM
            new PitchPosition(9, 0.0, -0.5),     // CAM
            new PitchPosition(10, 0.40, -0.10),  // CM
            new PitchPosition(11, 0.90, -0.30),  // RM

            new PitchPosition(1, -0.90, 0.40),   // LB
            new PitchPosition(2, -0.45, 0.50),   // CB
            new PitchPosition(3, 0.0, 0.55),     // CB
            new PitchPosition(4, 0.45, 0.50),    // CB
            new PitchPosition(5, 0.90, 0.40),    // RB

            new PitchPosition(0, 0.0, 1.0)       // GK
    );

    private static final Map<Integer, PitchPosition> POSITION_MAP =
            PITCH_POSITIONS.stream()
                    .collect(Collectors.toMap(PitchPosition::slotIndex, p -> p));

    public TeamChemistryResponseDto calculateTeamChemistry(UUID teamId) {
        List<TeamMember> starters = teamMemberRepository.findByTeamIdAndSquadType(teamId, SquadType.PITCH);

        if (starters.isEmpty()) return new TeamChemistryResponseDto(0, List.of());

        Map<Integer, UUID> slotToUser = new HashMap<>();
        for (TeamMember m : starters) slotToUser.put(m.getSlotIndex(), m.getUser().getId());

        Set<PlayerPair> pairs = generateLinks(slotToUser);

        List<TeamChemistryLinkDto> links = new ArrayList<>();
        Map<PlayerPair, Integer> cache = new HashMap<>();
        double sum = 0;

        for (PlayerPair pair : pairs) {
            int chemistry = cache.computeIfAbsent(pair, p -> chemistryService.compute(pair.a(), pair.b()).score());
            links.add(new TeamChemistryLinkDto(pair.a(), pair.b(), chemistry));
            sum += chemistry;
        }

        int overall = pairs.isEmpty() ? 0 : (int) Math.round(sum / pairs.size());
        return new TeamChemistryResponseDto(overall, links);
    }

    private Set<PlayerPair> generateLinks(Map<Integer, UUID> slotToUser) {
        List<Node> nodes = new ArrayList<>();
        for (var e : slotToUser.entrySet()) {
            PitchPosition pos = POSITION_MAP.get(e.getKey());
            if (pos != null) {
                nodes.add(new Node(e.getValue(), pos.x(), pos.y(), identifyLayer(pos.y())));
            }
        }

        Set<PlayerPair> pairs = new HashSet<>();
        Map<Integer, List<Node>> layersMap = nodes.stream().collect(Collectors.groupingBy(n -> n.layer));

        // 1. Legături Orizontale
        layersMap.forEach((layerIdx, playersInLayer) -> {
            playersInLayer.sort(Comparator.comparingDouble(n -> n.x));
            for (int i = 0; i < playersInLayer.size() - 1; i++) {
                pairs.add(PlayerPair.of(playersInLayer.get(i).user, playersInLayer.get(i + 1).user));
            }
        });

        // 2. Legături Verticale (Strat cu Strat)
        List<Integer> sortedLayers = layersMap.keySet().stream().sorted().toList();
        for (int i = 0; i < sortedLayers.size() - 1; i++) {
            List<Node> currentLayer = layersMap.get(sortedLayers.get(i));
            List<Node> nextLayer = layersMap.get(sortedLayers.get(i + 1));

            for (Node p1 : currentLayer) {
                nextLayer.stream()
                        .filter(p2 -> Math.abs(p1.x - p2.x) < 0.75) // Prag pt diagonale
                        .sorted(Comparator.comparingDouble(p2 -> Math.abs(p1.x - p2.x)))
                        .limit(2) // Max 2 legături verticale per jucător către stratul următor
                        .forEach(p2 -> pairs.add(PlayerPair.of(p1.user, p2.user)));
            }
        }
        return pairs;
    }

    private int identifyLayer(double y) {
        if (y > 0.80) return 0;  // GK
        if (y >= 0.35) return 1; // DEF (LB, CB, RB)
        if (y > 0.05)  return 2; // CDM (y=0.1)
        if (y >= -0.4) return 3; // MID (LM, CM, RM la -0.1, -0.3)
        if (y >= -0.7) return 4; // CAM (y=-0.5)
        return 5;                // ATK (LW, ST, RW la -0.85, -0.95)
    }

    private static class Node {
        UUID user; double x; double y; int layer;
        Node(UUID user, double x, double y, int layer) {
            this.user = user; this.x = x; this.y = y; this.layer = layer;
        }
    }
}