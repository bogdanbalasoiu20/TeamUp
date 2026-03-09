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

    // Coordonate aliniate 1:1 cu Frontend-ul tău
    private static final List<PitchPosition> PITCH_POSITIONS = List.of(
            // --- ATACANȚI ---
            new PitchPosition(12, -0.8, -0.85),
            new PitchPosition(13, -0.25, -0.95),
            new PitchPosition(14, 0.25, -0.95),
            new PitchPosition(15, 0.8, -0.85),

            // --- MIJLOCAȘI ---
            new PitchPosition(6, -0.90, -0.30),
            new PitchPosition(7, -0.40, -0.10),
            new PitchPosition(8, 0.0, 0.1),
            new PitchPosition(9, 0.0, -0.5),
            new PitchPosition(10, 0.40, -0.10),
            new PitchPosition(11, 0.90, -0.30),

            // --- FUNDAȘI ---
            new PitchPosition(1, -0.90, 0.40),
            new PitchPosition(2, -0.45, 0.50),
            new PitchPosition(3, 0.0, 0.55),
            new PitchPosition(4, 0.45, 0.50),
            new PitchPosition(5, 0.90, 0.40),

            // --- PORTAR ---
            new PitchPosition(0, 0.0, 1.0)
    );

    private static final Map<Integer, PitchPosition> POSITION_MAP =
            PITCH_POSITIONS.stream()
                    .collect(Collectors.toMap(PitchPosition::slotIndex, p -> p));

    public TeamChemistryResponseDto calculateTeamChemistry(UUID teamId) {
        List<TeamMember> starters =
                teamMemberRepository.findByTeamIdAndSquadType(teamId, SquadType.PITCH);

        if (starters.isEmpty())
            return new TeamChemistryResponseDto(0, List.of());

        Map<Integer, UUID> slotToUser = new HashMap<>();
        for (TeamMember m : starters)
            slotToUser.put(m.getSlotIndex(), m.getUser().getId());

        Set<PlayerPair> pairs = generateLinks(slotToUser);

        List<TeamChemistryLinkDto> links = new ArrayList<>();
        Map<PlayerPair, Integer> cache = new HashMap<>();
        double sum = 0;

        for (PlayerPair pair : pairs) {
            int chemistry = cache.computeIfAbsent(
                    pair,
                    p -> chemistryService.compute(pair.a(), pair.b()).score()
            );
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
                int layer = identifyLayer(pos.y());
                nodes.add(new Node(e.getValue(), pos.x(), pos.y(), layer));
            }
        }

        Set<PlayerPair> pairs = new HashSet<>();
        Map<Integer, List<Node>> layersMap = nodes.stream()
                .collect(Collectors.groupingBy(n -> n.layer));

        // 1. Legături Orizontale (ex: CB cu CB, ST cu ST)
        layersMap.forEach((layerIdx, playersInLayer) -> {
            playersInLayer.sort(Comparator.comparingDouble(n -> n.x));
            for (int i = 0; i < playersInLayer.size() - 1; i++) {
                pairs.add(PlayerPair.of(playersInLayer.get(i).user, playersInLayer.get(i + 1).user));
            }
        });

        // 2. Legături Verticale între straturi (GK -> DEF -> MID -> ATK)
        List<Integer> sortedLayerIndices = layersMap.keySet().stream().sorted().toList();

        for (int i = 0; i < sortedLayerIndices.size() - 1; i++) {
            List<Node> currentLayerNodes = layersMap.get(sortedLayerIndices.get(i));
            List<Node> nextLayerNodes = layersMap.get(sortedLayerIndices.get(i + 1));

            for (Node p1 : currentLayerNodes) {
                nextLayerNodes.stream()
                        // Filtru distanță X: previne link-uri absurde de tip LB la RM
                        .filter(p2 -> Math.abs(p1.x - p2.x) < 0.7)
                        // Conectăm la cei mai apropiați 2 vecini din stratul următor
                        .sorted(Comparator.comparingDouble(p2 -> Math.abs(p1.x - p2.x)))
                        .limit(2)
                        .forEach(p2 -> pairs.add(PlayerPair.of(p1.user, p2.user)));
            }
        }

        return pairs;
    }

    /**
     * Grupează pozițiile în straturi orizontale pe baza coordonatei Y.
     * Valorile sunt alese pentru a cuprinde variațiile din Frontend.
     */
    private int identifyLayer(double y) {
        if (y > 0.75) return 0;      // Layer 0: Portar (y=1.0)
        if (y >= 0.35) return 1;     // Layer 1: Fundași (y=0.40 până la 0.55)
        if (y >= -0.60) return 2;    // Layer 2: Mijlocași (y=-0.50 până la 0.10)
        return 3;                    // Layer 3: Atacanți (y=-0.85 până la -0.95)
    }

    private static class Node {
        UUID user;
        double x;
        double y;
        int layer;

        Node(UUID user, double x, double y, int layer) {
            this.user = user;
            this.x = x;
            this.y = y;
            this.layer = layer;
        }
    }
}