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
            new PitchPosition(12,-0.8,-0.85),
            new PitchPosition(13,-0.25,-0.95),
            new PitchPosition(14,0.25,-0.95),
            new PitchPosition(15,0.8,-0.85),

            new PitchPosition(6,-0.90,-0.30),
            new PitchPosition(7,-0.40,-0.10),
            new PitchPosition(8,0.0,0.1),
            new PitchPosition(9,0.0,-0.5),
            new PitchPosition(10,0.40,-0.10),
            new PitchPosition(11,0.90,-0.30),

            new PitchPosition(1,-0.90,0.40),
            new PitchPosition(2,-0.45,0.50),
            new PitchPosition(3,0.0,0.55),
            new PitchPosition(4,0.45,0.50),
            new PitchPosition(5,0.90,0.40),

            new PitchPosition(0,0.0,1.0)
    );

    private static final Map<Integer, PitchPosition> POSITION_MAP =
            PITCH_POSITIONS.stream()
                    .collect(Collectors.toMap(PitchPosition::slotIndex, p -> p));

    public TeamChemistryResponseDto calculateTeamChemistry(UUID teamId){

        List<TeamMember> starters =
                teamMemberRepository.findByTeamIdAndSquadType(teamId, SquadType.PITCH);

        if(starters.isEmpty())
            return new TeamChemistryResponseDto(0, List.of());

        Map<Integer, UUID> slotToUser = new HashMap<>();

        for(TeamMember m : starters)
            slotToUser.put(m.getSlotIndex(), m.getUser().getId());

        Set<PlayerPair> pairs = generateLinks(slotToUser);

        List<TeamChemistryLinkDto> links = new ArrayList<>();
        Map<PlayerPair,Integer> cache = new HashMap<>();

        double sum = 0;

        for(PlayerPair pair : pairs){

            int chemistry = cache.computeIfAbsent(
                    pair,
                    p -> chemistryService.compute(pair.a(), pair.b()).score()
            );

            links.add(new TeamChemistryLinkDto(pair.a(), pair.b(), chemistry));

            sum += chemistry;
        }

        int overall = pairs.isEmpty() ? 0 : (int)Math.round(sum / pairs.size());

        return new TeamChemistryResponseDto(overall, links);
    }

    private Set<PlayerPair> generateLinks(Map<Integer, UUID> slotToUser){

        List<Node> nodes = new ArrayList<>();

        for(var e : slotToUser.entrySet()){

            PitchPosition pos = POSITION_MAP.get(e.getKey());

            if(pos == null)
                continue;

            nodes.add(new Node(e.getValue(), pos.x(), pos.y()));
        }

        Set<PlayerPair> pairs = new HashSet<>();
        List<Edge> edges = new ArrayList<>();

        // LINKURI PE LINIE (OX)
        Map<Double,List<Node>> rows = nodes.stream()
                .collect(Collectors.groupingBy(n -> n.y));

        for(List<Node> row : rows.values()){

            row.sort(Comparator.comparingDouble(n -> n.x));

            for(int i=0;i<row.size()-1;i++){

                Node a = row.get(i);
                Node b = row.get(i+1);

                addEdge(a,b,pairs,edges);
            }
        }

        // LINKURI VERTICALE (OY)
        for(Node a : nodes){

            double bestDy = Double.MAX_VALUE;
            List<Node> candidates = new ArrayList<>();

            for(Node b : nodes){

                if(b.y >= a.y)
                    continue;

                double dx = Math.abs(a.x - b.x);

                // FILTRU IMPORTANT -> evita legaturi laterale absurde
                if(dx > 0.45)
                    continue;

                double dy = a.y - b.y;

                if(dy < bestDy - 0.01){

                    candidates.clear();
                    candidates.add(b);
                    bestDy = dy;

                }else if(Math.abs(dy - bestDy) < 0.01){

                    candidates.add(b);
                }
            }

            for(Node b : candidates)
                addEdge(a,b,pairs,edges);
        }

        return pairs;
    }

    private void addEdge(Node a, Node b,
                         Set<PlayerPair> pairs,
                         List<Edge> edges){

        Edge newEdge = new Edge(a,b);

        for(Edge e : edges){

            if(intersects(newEdge,e))
                return;
        }

        edges.add(newEdge);
        pairs.add(PlayerPair.of(a.user,b.user));
    }

    private boolean intersects(Edge e1, Edge e2){

        return linesIntersect(
                e1.a.x, e1.a.y,
                e1.b.x, e1.b.y,
                e2.a.x, e2.a.y,
                e2.b.x, e2.b.y
        );
    }

    private boolean linesIntersect(
            double x1,double y1,
            double x2,double y2,
            double x3,double y3,
            double x4,double y4){

        double d1 = direction(x3,y3,x4,y4,x1,y1);
        double d2 = direction(x3,y3,x4,y4,x2,y2);
        double d3 = direction(x1,y1,x2,y2,x3,y3);
        double d4 = direction(x1,y1,x2,y2,x4,y4);

        return d1*d2 < 0 && d3*d4 < 0;
    }

    private double direction(
            double xi,double yi,
            double xj,double yj,
            double xk,double yk){

        return (xk-xi)*(yj-yi)-(xj-xi)*(yk-yi);
    }

    private static class Node{

        UUID user;
        double x;
        double y;

        Node(UUID user,double x,double y){
            this.user = user;
            this.x = x;
            this.y = y;
        }
    }

    private static class Edge{

        Node a;
        Node b;

        Edge(Node a,Node b){
            this.a = a;
            this.b = b;
        }
    }
}