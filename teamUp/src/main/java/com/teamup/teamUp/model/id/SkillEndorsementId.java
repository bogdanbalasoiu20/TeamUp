package com.teamup.teamUp.model.id;

import jakarta.persistence.Column;
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
    @Column(name = "target_user_id", nullable = false, insertable = false, updatable = false)
    private UUID targetUserId;

    @Column(name = "voter_user_id", nullable = false, insertable = false, updatable = false)
    private UUID voterUserId;

    @Column(name = "skill_code",  nullable = false, insertable = false, updatable = false)
    private String skillCode;
}
