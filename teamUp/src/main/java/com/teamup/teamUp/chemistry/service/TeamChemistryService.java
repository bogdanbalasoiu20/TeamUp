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
        for(Map.Entry<Integer,List<Node>> entry : layersMap.entrySet()){

            int layer = entry.getKey();
            List<Node> layerNodes = entry.getValue();

            layerNodes.sort(Comparator.comparingDouble(n->n.x));

            for(int i=0;i<layerNodes.size()-1;i++){

                Node left=layerNodes.get(i);
                Node right=layerNodes.get(i+1);

                // ❌ blocăm LM ↔ RM
                if(layer==3 && Math.abs(left.x)>0.6 && Math.abs(right.x)>0.6)
                    continue;

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

        // LW ↔ LM (mereu)
        attackers.stream().filter(a->a.x<-0.6).findFirst().ifPresent(lw->
                mids.stream().filter(m->m.x<-0.6).findFirst().ifPresent(lm->
                        pairs.add(PlayerPair.of(lw.user,lm.user))
                ));

        // RW ↔ RM (mereu)
        attackers.stream().filter(a->a.x>0.6).findFirst().ifPresent(rw->
                mids.stream().filter(m->m.x>0.6).findFirst().ifPresent(rm->
                        pairs.add(PlayerPair.of(rw.user,rm.user))
                ));

        // CAM links
        if(hasCAM){

            Node cam = cams.get(0);

            // CAM ↔ LW RW LM RM
            nodes.stream()
                    .filter(n -> n.layer==3 || n.layer==5)
                    .filter(n -> Math.abs(n.x) > 0.35)
                    .forEach(n -> pairs.add(PlayerPair.of(cam.user,n.user)));

            // 🔥 CAM ↔ ST (ambele)
            attackers.stream()
                    .filter(a -> Math.abs(a.x) < 0.5)
                    .forEach(st ->
                            pairs.add(PlayerPair.of(cam.user,st.user))
                    );


            long cmCount = mids.stream()
                    .filter(m -> Math.abs(m.x) < 0.4)
                    .count();

            if(cmCount <= 1){
                nodes.stream()
                        .filter(n -> n.layer==2 && Math.abs(n.x) < 0.4)
                        .forEach(cdm ->
                                pairs.add(PlayerPair.of(cam.user, cdm.user))
                        );
            }
        }

        // CM ↔ ST dacă nu există CAM
        if(!hasCAM){

            List<Node> cms = mids.stream()
                    .filter(m -> Math.abs(m.x) < 0.5)
                    .toList();

            List<Node> sts = attackers.stream()
                    .filter(a -> Math.abs(a.x) < 0.5)
                    .toList();

            if(cms.isEmpty() || sts.isEmpty())
                return;

            // sortare stânga → dreapta
            List<Node> cmsSorted = cms.stream()
                    .sorted(Comparator.comparingDouble(n -> n.x))
                    .toList();

            List<Node> stsSorted = sts.stream()
                    .sorted(Comparator.comparingDouble(n -> n.x))
                    .toList();

            // 1 ST
            if(stsSorted.size()==1){

                Node st = stsSorted.get(0);

                cmsSorted.forEach(cm ->
                        pairs.add(PlayerPair.of(cm.user, st.user))
                );
            }

            // 2 ST
            else{

                // 1 CM
                if(cmsSorted.size()==1){

                    Node cm = cmsSorted.get(0);

                    stsSorted.forEach(st ->
                            pairs.add(PlayerPair.of(cm.user, st.user))
                    );
                }

                // 2 CM sau mai multe
                else{

                    for(int i=0;i<cmsSorted.size();i++){

                        Node cm = cmsSorted.get(i);

                        Node st = stsSorted.get(
                                Math.min(i, stsSorted.size()-1)
                        );

                        pairs.add(PlayerPair.of(cm.user, st.user));
                    }
                }
            }
        }


        // CDM ↔ LM dacă nu există CM stânga
        // CDM ↔ LM/RM rules
        nodes.stream()
                .filter(n -> n.layer == 2 && Math.abs(n.x) < 0.4)
                .findFirst()
                .ifPresent(cdm -> {

                    long cmCount = mids.stream()
                            .filter(m -> Math.abs(m.x) < 0.5)
                            .count();

                    Optional<Node> lm = mids.stream()
                            .filter(m -> m.x < -0.6)
                            .findFirst();

                    Optional<Node> rm = mids.stream()
                            .filter(m -> m.x > 0.6)
                            .findFirst();

                    // caz 1: nu există CM deloc → link cu ambele aripi
                    if(cmCount == 0) {

                        lm.ifPresent(n ->
                                pairs.add(PlayerPair.of(cdm.user, n.user))
                        );

                        rm.ifPresent(n ->
                                pairs.add(PlayerPair.of(cdm.user, n.user))
                        );

                        return;
                    }

                    // caz 2: există CM stânga
                    boolean hasLeftCM = mids.stream()
                            .anyMatch(m -> m.x < -0.2 && Math.abs(m.x) < 0.5);

                    // caz 3: există CM dreapta
                    boolean hasRightCM = mids.stream()
                            .anyMatch(m -> m.x > 0.2 && Math.abs(m.x) < 0.5);

                    if(hasLeftCM && rm.isPresent())
                        pairs.add(PlayerPair.of(cdm.user, rm.get().user));

                    if(hasRightCM && lm.isPresent())
                        pairs.add(PlayerPair.of(cdm.user, lm.get().user));
                });


        // CB stânga ↔ LM dacă nu există LB
        defenders.stream()
                .filter(d -> d.x < -0.2 && Math.abs(d.x) < 0.6) // CB stânga
                .findFirst()
                .ifPresent(cbLeft -> {

                    boolean hasLB = defenders.stream().anyMatch(d -> d.x < -0.6);

                    if(!hasLB){
                        mids.stream()
                                .filter(m -> m.x < -0.6)
                                .findFirst()
                                .ifPresent(lm ->
                                        pairs.add(PlayerPair.of(cbLeft.user, lm.user))
                                );
                    }
                });


        // CB dreapta ↔ RM dacă nu există RB
        defenders.stream()
                .filter(d -> d.x > 0.2 && Math.abs(d.x) < 0.6) // CB dreapta
                .findFirst()
                .ifPresent(cbRight -> {

                    boolean hasRB = defenders.stream().anyMatch(d -> d.x > 0.6);

                    if(!hasRB){
                        mids.stream()
                                .filter(m -> m.x > 0.6)
                                .findFirst()
                                .ifPresent(rm ->
                                        pairs.add(PlayerPair.of(cbRight.user, rm.user))
                                );
                    }
                });



        // CDM ↔ CM (mereu)
        nodes.stream()
                .filter(n -> n.layer == 2 && Math.abs(n.x) < 0.4)
                .findFirst()
                .ifPresent(cdm -> {

                    mids.stream()
                            .filter(cm -> cm.x > -0.5 && cm.x < 0.5) // toate CM
                            .forEach(cm ->
                                    pairs.add(PlayerPair.of(cdm.user, cm.user))
                            );
                });


        // CB central links
        defenders.stream()
                .filter(d -> Math.abs(d.x) < 0.2) // CB central
                .findFirst()
                .ifPresent(cbCenter -> {

                    Optional<Node> cdmOpt = nodes.stream()
                            .filter(n -> n.layer == 2 && Math.abs(n.x) < 0.4)
                            .findFirst();

                    if(cdmOpt.isPresent()) {

                        // dacă există CDM → CB central ↔ CDM
                        Node cdm = cdmOpt.get();
                        pairs.add(PlayerPair.of(cbCenter.user, cdm.user));

                    } else {

                        // dacă NU există CDM → CB central ↔ CM
                        mids.stream()
                                .filter(cm -> Math.abs(cm.x) < 0.5)
                                .forEach(cm ->
                                        pairs.add(PlayerPair.of(cbCenter.user, cm.user))
                                );
                    }
                });
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