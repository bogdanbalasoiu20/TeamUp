package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.card.PlayerCardDto;
import com.teamup.teamUp.model.entity.PlayerCardStats;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.Position;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PlayerCardMapper {
    public PlayerCardDto mapToDto(PlayerCardStats s, User u) {
        boolean isGk = u.getPosition() == Position.GOALKEEPER;

        return new PlayerCardDto(
                s.getUserId(),
                u.getUsername(),
                u.getPosition(),
                (int) Math.round(s.getOverallRating()),
                u.getPhotoUrl(),
                isGk
                        ? Map.of(
                        "DIV", round(s.getGkDiving()),
                        "HAN", round(s.getGkHandling()),
                        "KIC", round(s.getGkKicking()),
                        "REF", round(s.getGkReflexes()),
                        "SPD", round(s.getGkSpeed()),
                        "POS", round(s.getGkPositioning())
                )
                        : Map.of(
                        "PAC", round(s.getPace()),
                        "SHO", round(s.getShooting()),
                        "PAS", round(s.getPassing()),
                        "DRI", round(s.getDribbling()),
                        "DEF", round(s.getDefending()),
                        "PHY", round(s.getPhysical())
                )
        );
    }

    private int round(Double v) {
        return v == null ? 0 : (int) Math.round(v);
    }

}
