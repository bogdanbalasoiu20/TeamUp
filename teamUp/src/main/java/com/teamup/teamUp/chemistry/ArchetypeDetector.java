package com.teamup.teamUp.chemistry;

import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.enums.PlayerArchetype;
import com.teamup.teamUp.model.enums.Position;
import org.springframework.stereotype.Component;

@Component
public class ArchetypeDetector {

    public PlayerArchetype detect(PlayerCardStats stats, Position position) {
        if (position == null || stats == null) {
            return PlayerArchetype.BALANCED;
        }

        //Calculam atributele dominante (Top 2)
        // Asta rezolva problema: functioneaza si pentru rating 70, si pentru 99.
        return resolveArchetype(stats, position);
    }

    private PlayerArchetype resolveArchetype(PlayerCardStats stats, Position position) {
        double pace = stats.getPace();
        double shoot = stats.getShooting();
        double pass = stats.getPassing();
        double drib = stats.getDribbling();
        double def = stats.getDefending();
        double phys = stats.getPhysical();

        switch (position) {
            case FORWARD:
                // SPEEDSTER: Viteza este atributul dominant SAU (Viteza e mare si Driblingul e al doilea)
                if (pace >= phys && pace >= shoot) {
                    return PlayerArchetype.SPEEDSTER;
                }
                // TARGET MAN: Fizicul este dominant sau foarte apropiat de maxim, si Shooting bun
                if (phys >= pace && phys >= pass) {
                    return PlayerArchetype.TARGET_MAN;
                }
                // POACHER: Shooting este maximul absolut
                if (shoot >= pace && shoot >= pass && shoot >= phys) {
                    return PlayerArchetype.POACHER;
                }
                // Fallback pentru atacanti tehnici (ex: False 9)
                if (pass >= phys && drib >= phys) {
                    return PlayerArchetype.PLAYMAKER;
                }
                return PlayerArchetype.BALANCED;

            case MIDFIELDER:
                // DESTROYER: Defensiva si Fizicul domina net Pasele
                if (def >= pass && phys >= drib) {
                    return PlayerArchetype.DESTROYER;
                }
                // PLAYMAKER: Pasele si Driblingul domina Defensiva si Fizicul
                if (pass >= def && drib >= phys) {
                    return PlayerArchetype.PLAYMAKER;
                }
                // BOX TO BOX: Totul e echilibrat (diferenta mica intre cel mai bun si cel mai slab stat)
                // Verificam daca stats-urile sunt "plate"
                double maxStat = Math.max(pass, Math.max(def, phys));
                double minStat = Math.min(pass, Math.min(def, phys));
                if ((maxStat - minStat) < 15.0) {
                    return PlayerArchetype.BOX_TO_BOX;
                }

                // Default logic daca iese un stat in evidenta
                if (def > pass) return PlayerArchetype.DESTROYER;
                return PlayerArchetype.PLAYMAKER;

            case DEFENDER:
                // WING BACK: Viteza e mai mare decat Defensiva (Fouls logic, dar corect pt rol)
                if (pace > def) {
                    return PlayerArchetype.WING_BACK;
                }
                // BALL PLAYING: Pase bune (relativ la nivelul lui de fundas)
                // Adica pasele sunt comparabile cu defensiva
                if (pass >= (def - 10.0)) {
                    return PlayerArchetype.BALL_PLAYING_CB;
                }
                // STOPPER: Defensiva si Fizicul sunt regii
                return PlayerArchetype.STOPPER;

            case GOALKEEPER:
                // Aici e mai simplu: daca stie sa paseze (Pase > 70 sau Pase > Viteza * 2)
                if (pass > 65.0 || pass > phys) {
                    return PlayerArchetype.SWEEPER_KEEPER;
                }
                return PlayerArchetype.CLASSIC_GK;

            default:
                return PlayerArchetype.BALANCED;
        }
    }
}