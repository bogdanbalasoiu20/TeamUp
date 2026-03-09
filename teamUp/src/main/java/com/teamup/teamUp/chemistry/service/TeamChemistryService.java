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
                    .collect(Collectors.toMap(PitchPosition::slotIndex, p -> p));

    // axe tactice
    private static final Map<Integer, Integer> SLOT_AXIS = Map.ofEntries(
            Map.entry(0, 1), // GK

            Map.entry(1, 2),
            Map.entry(2, 2),
            Map.entry(3, 2),
            Map.entry(4, 2),
            Map.entry(5, 2),

            Map.entry(7, 3),
            Map.entry(8, 3),
            Map.entry(10, 3),

            Map.entry(6, 4),
            Map.entry(9, 4),
            Map.entry(11, 4),

            Map.entry(12, 5),
            Map.entry(13, 5),
            Map.entry(14, 5),
            Map.entry(15, 5)
    );

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

        Map<Integer, List<UUID>> axisPlayers = new HashMap<>();

        for (var entry : slotToUser.entrySet()) {

            Integer axis = SLOT_AXIS.get(entry.getKey());

            if (axis == null)
                continue;

            axisPlayers.computeIfAbsent(axis, k -> new ArrayList<>())
                    .add(entry.getValue());
        }

        Set<PlayerPair> pairs = new HashSet<>();

        for (var entry : slotToUser.entrySet()) {

            UUID user = entry.getValue();
            PitchPosition pos = POSITION_MAP.get(entry.getKey());
            int axis = SLOT_AXIS.get(entry.getKey());

            List<UUID> neighbors = findNeighborAxis(axisPlayers, axis);

            if (neighbors.isEmpty())
                continue;

            neighbors.stream()
                    .sorted(Comparator.comparingDouble(n ->
                            Math.abs(pos.x() - getPosition(slotToUser, n).x())))
                    .limit(2)
                    .forEach(n -> pairs.add(PlayerPair.of(user, n)));
        }

        return pairs;
    }

    private List<UUID> findNeighborAxis(Map<Integer, List<UUID>> axisPlayers, int axis) {

        for (int i = 1; i <= 4; i++) {

            List<UUID> up = axisPlayers.get(axis + i);
            if (up != null)
                return up;

            List<UUID> down = axisPlayers.get(axis - i);
            if (down != null)
                return down;
        }

        return List.of();
    }

    private PitchPosition getPosition(Map<Integer, UUID> slotToUser, UUID userId) {

        for (var entry : slotToUser.entrySet())
            if (entry.getValue().equals(userId))
                return POSITION_MAP.get(entry.getKey());

        return null;
    }
}