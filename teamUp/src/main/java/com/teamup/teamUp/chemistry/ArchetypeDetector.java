package com.teamup.teamUp.chemistry;

import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.enums.PlayerArchetype;
import com.teamup.teamUp.model.enums.Position;
import org.springframework.stereotype.Component;

@Component
public class ArchetypeDetector {

    public PlayerArchetype detect(PlayerCardStats s, Position position) {
        if (s == null || position == null) {
            return PlayerArchetype.BALANCED;
        }

        return switch (position) {
            case FORWARD -> detectForward(s);
            case MIDFIELDER -> detectMidfielder(s);
            case DEFENDER -> detectDefender(s);
            case GOALKEEPER -> detectGoalkeeper(s);
            default -> PlayerArchetype.BALANCED;
        };
    }

    //FORWARD

    private PlayerArchetype detectForward(PlayerCardStats s) {
        double speedster =norm( 1.0 * s.getPace() +
                        0.6 * s.getDribbling() +
                        0.4 * s.getPassing() +
                        0.3 * s.getShooting(),
                        1.0+0.6+0.4+0.3);

        double poacher =norm(1.0 * s.getShooting() +
                        0.6 * s.getDribbling() +
                        0.5 * s.getPassing() +
                        0.3 * s.getPace(),
                1.0+0.6+0.5+0.3);

        double targetMan =norm (1.0 * s.getPhysical() +
                        0.8 * s.getShooting() +
                        0.3 * s.getDribbling() +
                        0.2 * s.getPassing(),
                        1.0+0.8+0.3+0.2);

        double playmaker = norm(1.0 * s.getPassing() +
                        0.7 * s.getDribbling() +
                        0.4 * s.getPace() +
                        0.5 * s.getShooting(),
                1.0+0.7+0.4+0.5);

        return maxOf(
                speedster, PlayerArchetype.SPEEDSTER,
                poacher, PlayerArchetype.POACHER,
                targetMan, PlayerArchetype.TARGET_MAN,
                playmaker, PlayerArchetype.PLAYMAKER
        );
    }

    //MIDFIELDER

    private PlayerArchetype detectMidfielder(PlayerCardStats s) {

        double playmaker = norm(0.3 * s.getPace() +
                        1.0 * s.getPassing() +
                        0.9 * s.getDribbling() +
                        0.2 * s.getDefending() +
                        0.2 * s.getPhysical() +
                        0.7 * s.getShooting(),
                        0.3+1.0 + 0.9 + 0.2 + 0.2 + 0.7);

        double destroyer = norm(0.2 * s.getPace() +
                        0.5 * s.getPassing() +
                        0.2 * s.getDribbling() +
                        1.0 * s.getDefending() +
                        0.9 * s.getPhysical() +
                        0.3 * s.getShooting(),
                        0.2+0.5+0.2+1.0+0.9+0.3);

        double boxToBox = norm(0.6 * s.getPace() +
                        0.6 * s.getPassing() +
                        0.5 * s.getDribbling() +
                        0.7 * s.getDefending() +
                        0.6 * s.getPhysical() +
                        0.5 * s.getShooting(),
                        0.6 + 0.6 +0.5+0.7+0.6+0.5);

        return maxOf(
                playmaker, PlayerArchetype.PLAYMAKER,
                destroyer, PlayerArchetype.DESTROYER,
                boxToBox, PlayerArchetype.BOX_TO_BOX
        );
    }


    //DEFENDER

    private PlayerArchetype detectDefender(PlayerCardStats s) {
        double stopper =norm( 1.0 * s.getDefending() +
                        0.8 * s.getPhysical() +
                        0.2 * s.getPassing(),
                        1.0+0.8+0.2);

        double ballPlaying =norm( 0.9 * s.getDefending() +
                        0.6 * s.getPassing() +
                        0.5 *  s.getPhysical(),
                        0.9+0.6+0.5);

        double wingBack = norm(1.0 * s.getPace() +
                        0.6 * s.getPassing() +
                        0.5 * s.getDefending(),
                        1.0+0.6+0.5);

        return maxOf(
                stopper, PlayerArchetype.STOPPER,
                ballPlaying, PlayerArchetype.BALL_PLAYING_CB,
                wingBack, PlayerArchetype.WING_BACK
        );
    }

    //GOALKEEPER

    private PlayerArchetype detectGoalkeeper(PlayerCardStats s) {
        double classic =norm( 1.0 * s.getGkReflexes() +
                        0.8 * s.getGkPositioning() +
                        0.5 * s.getGkDiving(),
                        1.0+0.8+0.5);

        double sweeper =norm( 1.0 * s.getPassing() +
                        0.6 * s.getGkSpeed() +
                        0.3 * s.getGkPositioning(),
                1.0+0.6+0.3);


        return maxOf(
                classic, PlayerArchetype.CLASSIC_GK,
                sweeper, PlayerArchetype.SWEEPER_KEEPER
        );
    }

    //UTILS

    private PlayerArchetype maxOf(Object... values) {
        double max = Double.NEGATIVE_INFINITY;
        PlayerArchetype best = PlayerArchetype.BALANCED;

        for (int i = 0; i < values.length; i += 2) {
            double score = (double) values[i];
            PlayerArchetype type = (PlayerArchetype) values[i + 1];

            if (score > max) {
                max = score;
                best = type;
            }
        }

        return best;
    }


    private double norm(double score, double totalWeight) {
        return score / totalWeight;
    }


}
