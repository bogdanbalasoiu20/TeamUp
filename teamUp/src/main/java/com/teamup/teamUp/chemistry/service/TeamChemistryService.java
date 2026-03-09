package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.PitchPosition;
import com.teamup.teamUp.chemistry.dto.TeamChemistryLinkDto;
import com.teamup.teamUp.chemistry.dto.TeamChemistryResponseDto;
import com.teamup.teamUp.model.entity.TeamMember;
import com.teamup.teamUp.model.enums.SquadType;
import com.teamup.teamUp.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
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

        double weightedSum = 0;
        double totalWeight = 0;

        for (PlayerPair pair : pairs) {

            int chemistry = cache.computeIfAbsent(
                    pair,
                    p -> chemistryService.compute(pair.a(), pair.b()).score()
            );

            links.add(new TeamChemistryLinkDto(pair.a(), pair.b(), chemistry));

            weightedSum += chemistry;
            totalWeight += 1;
        }

        if (totalWeight == 0)
            return new TeamChemistryResponseDto(0, List.of());

        int overall = (int) Math.round(weightedSum / totalWeight);

        return new TeamChemistryResponseDto(overall, links);
    }

    private Set<PlayerPair> generateLinks(Map<Integer, UUID> slotToUser) {

        Map<Coordinate, UUID> coordinateMap = new HashMap<>();

        for (var entry : slotToUser.entrySet()) {

            PitchPosition pos = POSITION_MAP.get(entry.getKey());

            if (pos == null)
                continue;

            Coordinate coord = new Coordinate(pos.x(), pos.y());

            coordinateMap.put(coord, entry.getValue());
        }

        List<Coordinate> coordinates = new ArrayList<>(coordinateMap.keySet());

        if (coordinates.size() < 3) {

            Set<PlayerPair> pairs = new HashSet<>();

            for (int i = 0; i < coordinates.size(); i++) {
                for (int j = i + 1; j < coordinates.size(); j++) {

                    UUID a = coordinateMap.get(coordinates.get(i));
                    UUID b = coordinateMap.get(coordinates.get(j));

                    pairs.add(PlayerPair.of(a, b));
                }
            }

            return pairs;
        }

        GeometryFactory geometryFactory = new GeometryFactory();

        DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
        builder.setSites(coordinates);

        Geometry triangles = builder.getTriangles(geometryFactory);

        Set<PlayerPair> pairs = new HashSet<>();

        for (int i = 0; i < triangles.getNumGeometries(); i++) {

            Polygon triangle = (Polygon) triangles.getGeometryN(i);

            Coordinate[] coords = triangle.getCoordinates();

            for (int j = 0; j < coords.length - 1; j++) {

                Coordinate a = coords[j];
                Coordinate b = coords[(j + 1) % (coords.length - 1)];

                UUID userA = coordinateMap.get(a);
                UUID userB = coordinateMap.get(b);

                if (userA == null || userB == null)
                    continue;

                if (!userA.equals(userB))
                    pairs.add(PlayerPair.of(userA, userB));
            }
        }

        return pairs;
    }
}