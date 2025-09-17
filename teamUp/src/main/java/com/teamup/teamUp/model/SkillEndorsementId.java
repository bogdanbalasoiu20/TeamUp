package com.teamup.teamUp.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable  //marcheaza o clasa care nu este o entitatea de sine statatoare, ci se incorporeaza intr-o alta entitate. folosit pentru a marca o cheie compusa
@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillEndorsementId implements Serializable {
    private UUID targetUserId;
    private UUID voterUserId;
    private String skillCode;
}
