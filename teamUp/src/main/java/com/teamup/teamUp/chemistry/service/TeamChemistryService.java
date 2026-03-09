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
            new PitchPosition(12, -0.8, -0.85),  new PitchPosition(13, -0.25, -0.95),
            new PitchPosition(14, 0.25, -0.95),  new PitchPosition(15, 0.8, -0.85),
            new PitchPosition(6, -0.90, -0.30),  new PitchPosition(7, -0.40, -0.10),
            new PitchPosition(8, 0.0, 0.1),      new PitchPosition(9, 0.0, -0.5),
            new PitchPosition(10, 0.40, -0.10),  new PitchPosition(11, 0.90, -0.30),
            new PitchPosition(1, -0.90, 0.40),   new PitchPosition(2, -0.45, 0.50),
            new PitchPosition(3, 0.0, 0.55),     new PitchPosition(4, 0.45, 0.50),
            new PitchPosition(5, 0.90, 0.40),    new PitchPosition(0, 0.0, 1.0)
    );

    private static final Map<Integer, PitchPosition> POSITION_MAP =
            PITCH_POSITIONS.stream().collect(Collectors.toMap(PitchPosition::slotIndex, p -> p));

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
            if (pos != null) nodes.add(new Node(e.getValue(), pos.x(), pos.y(), identifyLayer(pos.y())));
        }

        Set<PlayerPair> pairs = new HashSet<>();
        Map<Integer, List<Node>> layersMap = nodes.stream().collect(Collectors.groupingBy(n -> n.layer));

        // 1. Legături Orizontale
        layersMap.forEach((idx, list) -> {
            list.sort(Comparator.comparingDouble(n -> n.x));
            for (int i = 0; i < list.size() - 1; i++) pairs.add(PlayerPair.of(list.get(i).user, list.get(i + 1).user));
        });

        // 2. Reguli Verticale și Logica Tactica
        List<Integer> sortedLayers = layersMap.keySet().stream().sorted().toList();
        for (int i = 0; i < sortedLayers.size(); i++) {
            int currentLayerIdx = sortedLayers.get(i);
            List<Node> currentLayer = layersMap.get(currentLayerIdx);

            for (Node p1 : currentLayer) {
                // REGULĂ GK: Doar cu CB (x între -0.5 și 0.5)
                if (p1.layer == 0) {
                    nodes.stream()
                            .filter(p2 -> p2.layer == 1 && Math.abs(p2.x) <= 0.5)
                            .forEach(p2 -> pairs.add(PlayerPair.of(p1.user, p2.user)));
                    continue;
                }

                // REGULĂ CM -> LW/RW (Legătură diagonală pe flanc)
                if (p1.layer == 3 && Math.abs(p1.x) > 0.3 && Math.abs(p1.x) < 0.6) {
                    nodes.stream()
                            .filter(p2 -> p2.layer == 5 && Math.abs(p2.x) > 0.6 && Math.signum(p1.x) == Math.signum(p2.x))
                            .forEach(p2 -> pairs.add(PlayerPair.of(p1.user, p2.user)));
                }

                // REGULĂ FLANC (LB -> LM/LW sau RB -> RM/RW)
                if (Math.abs(p1.x) > 0.6) {
                    nodes.stream()
                            .filter(p2 -> p2.layer > p1.layer && Math.abs(p2.x) > 0.5 && Math.signum(p1.x) == Math.signum(p2.x))
                            .min(Comparator.comparingDouble(p2 -> Math.abs(p1.y - p2.y)))
                            .ifPresent(p2 -> pairs.add(PlayerPair.of(p1.user, p2.user)));
                }

                // REGULĂ SPECIALĂ CDM -> CAM (Bypass dacă rândul de CM/LM/RM lipsește)
                if (p1.layer == 2 && !layersMap.containsKey(3)) {
                    nodes.stream()
                            .filter(p2 -> p2.layer == 4)
                            .forEach(p2 -> pairs.add(PlayerPair.of(p1.user, p2.user)));
                }

                // REGULĂ STANDARD STRATIFICATĂ
                if (i + 1 < sortedLayers.size()) {
                    layersMap.get(sortedLayers.get(i + 1)).stream()
                            .filter(p2 -> Math.abs(p1.x - p2.x) < 0.7)
                            .sorted(Comparator.comparingDouble(p2 -> Math.abs(p1.x - p2.x)))
                            .limit(2).forEach(p2 -> pairs.add(PlayerPair.of(p1.user, p2.user)));
                }
            }
        }
        return pairs;
    }

    private int identifyLayer(double y) {
        if (y > 0.80) return 0;  // GK
        if (y >= 0.35) return 1; // DEF
        if (y > 0.05) return 2;  // CDM
        if (y >= -0.4) return 3; // MID (CM, LM, RM)
        if (y >= -0.7) return 4; // CAM
        return 5;                // ATK
    }

    private static class Node {
        UUID user; double x; double y; int layer;
        Node(UUID user, double x, double y, int layer) {
            this.user = user; this.x = x; this.y = y; this.layer = layer;
        }
    }
}