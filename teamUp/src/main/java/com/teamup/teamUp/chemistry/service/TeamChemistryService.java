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
            new PitchPosition(12, -0.8, -0.85), new PitchPosition(13, -0.25, -0.95),
            new PitchPosition(14, 0.25, -0.95), new PitchPosition(15, 0.8, -0.85),

            new PitchPosition(6, -0.90, -0.30), new PitchPosition(7, -0.40, -0.10),
            new PitchPosition(8, 0.0, 0.1), new PitchPosition(9, 0.0, -0.5),
            new PitchPosition(10, 0.40, -0.10), new PitchPosition(11, 0.90, -0.30),

            new PitchPosition(1, -0.90, 0.40), new PitchPosition(2, -0.45, 0.50),
            new PitchPosition(3, 0.0, 0.55), new PitchPosition(4, 0.45, 0.50),
            new PitchPosition(5, 0.90, 0.40),

            new PitchPosition(0, 0.0, 1.0)
    );

    private static final Map<Integer, PitchPosition> POSITION_MAP =
            PITCH_POSITIONS.stream().collect(Collectors.toMap(PitchPosition::slotIndex, p -> p));

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

            int chemistry = cache.computeIfAbsent(pair,
                    p -> chemistryService.compute(pair.a(), pair.b()).score());

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

        Map<Integer, List<Node>> layersMap =
                nodes.stream().collect(Collectors.groupingBy(n -> n.layer));

        List<Integer> sortedLayers =
                layersMap.keySet().stream().sorted().toList();

        /* ---------- BASE LINKS ---------- */

        // Horizontal neighbors
        for (List<Node> layerNodes : layersMap.values()) {

            layerNodes.sort(Comparator.comparingDouble(n -> n.x));

            for (int i = 0; i < layerNodes.size() - 1; i++) {

                Node left = layerNodes.get(i);
                Node right = layerNodes.get(i + 1);

                pairs.add(PlayerPair.of(left.user, right.user));
            }
        }

        // Vertical links
        for (int i = 0; i < sortedLayers.size() - 1; i++) {

            List<Node> currentLayer = layersMap.get(sortedLayers.get(i));
            List<Node> nextLayer = layersMap.get(sortedLayers.get(i + 1));

            for (Node p1 : currentLayer) {

                Node best = null;
                double bestDist = Double.MAX_VALUE;

                for (Node p2 : nextLayer) {

                    double dx = Math.abs(p1.x - p2.x);

                    if (dx > 0.55) continue;

                    if (Math.signum(p1.x) != Math.signum(p2.x)
                            && Math.abs(p1.x) > 0.35
                            && Math.abs(p2.x) > 0.35)
                        continue;

                    double dy = Math.abs(p1.y - p2.y);
                    double dist = dx * dx + dy * dy;

                    if (dist < bestDist) {
                        bestDist = dist;
                        best = p2;
                    }
                }

                if (best != null)
                    pairs.add(PlayerPair.of(p1.user, best.user));
            }
        }

        /* ---------- FALLBACK LINKS ---------- */

        addFallbackLinks(nodes, pairs);

        return pairs;
    }

    private void addFallbackLinks(List<Node> nodes, Set<PlayerPair> pairs) {

        List<Node> defenders = nodes.stream().filter(n -> n.layer == 1).toList();
        List<Node> mids = nodes.stream().filter(n -> n.layer == 3).toList();
        List<Node> attackers = nodes.stream().filter(n -> n.layer == 5).toList();

        boolean hasLM = mids.stream().anyMatch(n -> n.x < -0.6);
        boolean hasRM = mids.stream().anyMatch(n -> n.x > 0.6);

        boolean hasLW = attackers.stream().anyMatch(n -> n.x < -0.6);
        boolean hasRW = attackers.stream().anyMatch(n -> n.x > 0.6);

        // LB -> LW
        if (!hasLM) {
            defenders.stream()
                    .filter(d -> d.x < -0.6)
                    .findFirst()
                    .ifPresent(lb ->
                            attackers.stream()
                                    .filter(a -> a.x < -0.6)
                                    .findFirst()
                                    .ifPresent(lw -> pairs.add(PlayerPair.of(lb.user, lw.user))));
        }

        // RB -> RW
        if (!hasRM) {
            defenders.stream()
                    .filter(d -> d.x > 0.6)
                    .findFirst()
                    .ifPresent(rb ->
                            attackers.stream()
                                    .filter(a -> a.x > 0.6)
                                    .findFirst()
                                    .ifPresent(rw -> pairs.add(PlayerPair.of(rb.user, rw.user))));
        }

        // LM -> ST
        if (!hasLW) {
            mids.stream()
                    .filter(m -> m.x < -0.6)
                    .findFirst()
                    .ifPresent(lm ->
                            attackers.stream()
                                    .filter(a -> Math.abs(a.x) < 0.3)
                                    .findFirst()
                                    .ifPresent(st -> pairs.add(PlayerPair.of(lm.user, st.user))));
        }

        // RM -> ST
        if (!hasRW) {
            mids.stream()
                    .filter(m -> m.x > 0.6)
                    .findFirst()
                    .ifPresent(rm ->
                            attackers.stream()
                                    .filter(a -> Math.abs(a.x) < 0.3)
                                    .findFirst()
                                    .ifPresent(st -> pairs.add(PlayerPair.of(rm.user, st.user))));
        }
    }

    private int identifyLayer(double y) {

        if (y > 0.80) return 0; // GK
        if (y >= 0.35) return 1; // DEF
        if (y > 0.05) return 2; // CDM
        if (y >= -0.4) return 3; // MID
        if (y >= -0.7) return 4; // CAM
        return 5; // ATK
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