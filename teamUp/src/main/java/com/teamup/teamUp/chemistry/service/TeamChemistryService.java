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
            new PitchPosition(12,-0.8,-0.85), new PitchPosition(13,-0.25,-0.95),
            new PitchPosition(14,0.25,-0.95), new PitchPosition(15,0.8,-0.85),

            new PitchPosition(6,-0.90,-0.30), new PitchPosition(7,-0.40,-0.10),
            new PitchPosition(8,0.0,0.1), new PitchPosition(9,0.0,-0.5),
            new PitchPosition(10,0.40,-0.10), new PitchPosition(11,0.90,-0.30),

            new PitchPosition(1,-0.90,0.40), new PitchPosition(2,-0.45,0.50),
            new PitchPosition(3,0.0,0.55), new PitchPosition(4,0.45,0.50),
            new PitchPosition(5,0.90,0.40),

            new PitchPosition(0,0.0,1.0)
    );

    private static final Map<Integer,PitchPosition> POSITION_MAP =
            PITCH_POSITIONS.stream().collect(Collectors.toMap(PitchPosition::slotIndex,p->p));

    public TeamChemistryResponseDto calculateTeamChemistry(UUID teamId){

        List<TeamMember> starters =
                teamMemberRepository.findByTeamIdAndSquadType(teamId,SquadType.PITCH);

        if(starters.isEmpty())
            return new TeamChemistryResponseDto(0,List.of());

        Map<Integer,UUID> slotToUser = new HashMap<>();

        for(TeamMember m: starters)
            slotToUser.put(m.getSlotIndex(),m.getUser().getId());

        Set<PlayerPair> pairs = generateLinks(slotToUser);

        List<TeamChemistryLinkDto> links = new ArrayList<>();

        Map<PlayerPair,Integer> cache = new HashMap<>();

        double sum=0;

        for(PlayerPair pair: pairs){

            int chemistry = cache.computeIfAbsent(pair,
                    p->chemistryService.compute(pair.a(),pair.b()).score());

            links.add(new TeamChemistryLinkDto(pair.a(),pair.b(),chemistry));

            sum+=chemistry;
        }

        int overall = pairs.isEmpty()?0:(int)Math.round(sum/pairs.size());

        return new TeamChemistryResponseDto(overall,links);
    }

    private Set<PlayerPair> generateLinks(Map<Integer,UUID> slotToUser){

        List<Node> nodes = new ArrayList<>();

        for(var e: slotToUser.entrySet()){
            PitchPosition pos = POSITION_MAP.get(e.getKey());
            if(pos!=null)
                nodes.add(new Node(e.getValue(),pos.x(),pos.y(),identifyLayer(pos.y())));
        }

        Set<PlayerPair> pairs = new HashSet<>();

        generateBaseLinks(nodes,pairs);

        addTacticalLinks(nodes,pairs);

        addFallbackLinks(nodes,pairs);

        return pairs;
    }

    /* ---------------- BASE LINKS ---------------- */

    private void generateBaseLinks(List<Node> nodes, Set<PlayerPair> pairs){

        Map<Integer,List<Node>> layersMap =
                nodes.stream().collect(Collectors.groupingBy(n->n.layer));

        List<Integer> sortedLayers =
                layersMap.keySet().stream().sorted().toList();

        // horizontal neighbors
        for(List<Node> layerNodes: layersMap.values()){

            layerNodes.sort(Comparator.comparingDouble(n->n.x));

            for(int i=0;i<layerNodes.size()-1;i++){

                Node left=layerNodes.get(i);
                Node right=layerNodes.get(i+1);

                pairs.add(PlayerPair.of(left.user,right.user));
            }
        }

        // vertical neighbors
        for(int i=0;i<sortedLayers.size()-1;i++){

            List<Node> currentLayer = layersMap.get(sortedLayers.get(i));
            List<Node> nextLayer = layersMap.get(sortedLayers.get(i+1));

            for(Node p1: currentLayer){

                Node best=null;
                double bestDist=Double.MAX_VALUE;

                for(Node p2: nextLayer){

                    double dx=Math.abs(p1.x-p2.x);

                    if(dx>0.55) continue;

                    if(Math.signum(p1.x)!=Math.signum(p2.x)
                            && Math.abs(p1.x)>0.35
                            && Math.abs(p2.x)>0.35)
                        continue;

                    double dy=Math.abs(p1.y-p2.y);
                    double dist=dx*dx+dy*dy;

                    if(dist<bestDist){
                        bestDist=dist;
                        best=p2;
                    }
                }

                if(best!=null)
                    pairs.add(PlayerPair.of(p1.user,best.user));
            }
        }
    }

    /* ---------------- TACTICAL LINKS ---------------- */

    private void addTacticalLinks(List<Node> nodes, Set<PlayerPair> pairs){

        List<Node> defenders = nodes.stream().filter(n->n.layer==1).toList();
        List<Node> mids = nodes.stream().filter(n->n.layer==3).toList();
        List<Node> attackers = nodes.stream().filter(n->n.layer==5).toList();
        List<Node> cams = nodes.stream().filter(n->n.layer==4).toList();

        boolean hasCAM = !cams.isEmpty();

        Node gk = nodes.stream().filter(n->n.layer==0).findFirst().orElse(null);

        // GK ↔ CB (toți)
        if(gk!=null){
            defenders.stream()
                    .filter(cb->Math.abs(cb.x)<0.6)
                    .forEach(cb->pairs.add(PlayerPair.of(gk.user,cb.user)));
        }

        // LB ↔ LM
        defenders.stream().filter(d->d.x<-0.6).findFirst().ifPresent(lb->
                mids.stream().filter(m->m.x<-0.6).findFirst().ifPresent(lm->
                        pairs.add(PlayerPair.of(lb.user,lm.user))
                ));

        // RB ↔ RM
        defenders.stream().filter(d->d.x>0.6).findFirst().ifPresent(rb->
                mids.stream().filter(m->m.x>0.6).findFirst().ifPresent(rm->
                        pairs.add(PlayerPair.of(rb.user,rm.user))
                ));

        // CAM ↔ aripi + mijlocași laterali
        if(hasCAM){
            Node cam = cams.get(0);

            nodes.stream()
                    .filter(n -> n.layer==3 || n.layer==5)
                    .filter(n -> Math.abs(n.x) > 0.35)
                    .forEach(n -> pairs.add(PlayerPair.of(cam.user,n.user)));
        }

        // CM ↔ ST dacă nu există CAM
        if(!hasCAM){

            List<Node> cms = mids.stream()
                    .filter(m -> Math.abs(m.x) < 0.4)
                    .toList();

            List<Node> sts = attackers.stream()
                    .filter(a -> Math.abs(a.x) < 0.5)
                    .toList();

            if(sts.size()==1){
                Node st = sts.get(0);
                cms.forEach(cm ->
                        pairs.add(PlayerPair.of(cm.user,st.user))
                );
            }
            else if(sts.size()==2){

                if(cms.size()==1){
                    Node cm = cms.get(0);
                    sts.forEach(st ->
                            pairs.add(PlayerPair.of(cm.user,st.user))
                    );
                }
                else{
                    for(Node cm : cms){

                        Node best = sts.stream()
                                .min(Comparator.comparingDouble(st -> Math.abs(st.x - cm.x)))
                                .orElse(null);

                        if(best!=null)
                            pairs.add(PlayerPair.of(cm.user,best.user));
                    }
                }
            }
        }
    }

    /* ---------------- FALLBACK LINKS ---------------- */

    private void addFallbackLinks(List<Node> nodes, Set<PlayerPair> pairs){

        List<Node> mids = nodes.stream().filter(n->n.layer==3).toList();
        List<Node> attackers = nodes.stream().filter(n->n.layer==5).toList();

        boolean hasLW = attackers.stream().anyMatch(n->n.x<-0.6);
        boolean hasRW = attackers.stream().anyMatch(n->n.x>0.6);

        if(!hasLW){
            mids.stream().filter(m->m.x<-0.6).findFirst().ifPresent(lm->
                    attackers.stream()
                            .min(Comparator.comparingDouble(a->Math.abs(a.x-lm.x)))
                            .ifPresent(st->pairs.add(PlayerPair.of(lm.user,st.user))));
        }

        if(!hasRW){
            mids.stream().filter(m->m.x>0.6).findFirst().ifPresent(rm->
                    attackers.stream()
                            .min(Comparator.comparingDouble(a->Math.abs(a.x-rm.x)))
                            .ifPresent(st->pairs.add(PlayerPair.of(rm.user,st.user))));
        }

        // LW ↔ LB dacă nu există LM
        nodes.stream().filter(n->n.layer==5 && n.x<-0.6).findFirst().ifPresent(lw->{
            boolean hasLM = nodes.stream().anyMatch(n->n.layer==3 && n.x<-0.6);
            if(!hasLM){
                nodes.stream().filter(n->n.layer==1 && n.x<-0.6).findFirst()
                        .ifPresent(lb -> pairs.add(PlayerPair.of(lb.user,lw.user)));
            }
        });

// RW ↔ RB dacă nu există RM
        nodes.stream().filter(n->n.layer==5 && n.x>0.6).findFirst().ifPresent(rw->{
            boolean hasRM = nodes.stream().anyMatch(n->n.layer==3 && n.x>0.6);
            if(!hasRM){
                nodes.stream().filter(n->n.layer==1 && n.x>0.6).findFirst()
                        .ifPresent(rb -> pairs.add(PlayerPair.of(rb.user,rw.user)));
            }
        });
    }

    private int identifyLayer(double y){

        if(y>0.80) return 0;
        if(y>=0.35) return 1;
        if(y>0.05) return 2;
        if(y>=-0.4) return 3;
        if(y>=-0.7) return 4;
        return 5;
    }

    private static class Node{

        UUID user;
        double x;
        double y;
        int layer;

        Node(UUID user,double x,double y,int layer){
            this.user=user;
            this.x=x;
            this.y=y;
            this.layer=layer;
        }
    }
}