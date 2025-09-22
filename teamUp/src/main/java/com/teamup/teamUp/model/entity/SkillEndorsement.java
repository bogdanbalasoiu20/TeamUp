package com.teamup.teamUp.model.entity;


import com.teamup.teamUp.model.id.SkillEndorsementId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "skill_endorsements")
@Getter
@Setter
@ToString(exclude = {"targetUser","voterUser","skill"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SkillEndorsement {
    @EmbeddedId  //spune ca PK nu e un singur camp, ci o clasa compusa
    @EqualsAndHashCode.Include //iau in calcul doar acest camp pentru equals si hashcode
    private SkillEndorsementId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("targetUserId") //ii spun lui Hibernate ca cheia externa target_user_id nu este doar o coloana ci face parte din cheia primara
    @JoinColumn(name = "target_user_id",nullable = false, foreignKey = @ForeignKey(name = "fk_se_target")) //se defineste fk, numele vine din constrangerea din postgres
    private User targetUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("voterUserId")
    @JoinColumn(name = "voter_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_se_voter"))
    private User voterUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("skillCode")
    @JoinColumn(name = "skill_code", nullable = false, foreignKey = @ForeignKey(name = "fk_se_skill"))
    private Skill skill;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    //cand incerc sa fac save(skillEndorsement), Hibernate va apela validateNotSelf() automat inainte sa ruleze INSERT sau UPDATE
    @PrePersist
    @PreUpdate
    private void validateNotSelf(){
        if(targetUser != null && voterUser != null && targetUser.getId().equals(voterUser.getId())){
            throw new IllegalArgumentException("User cannot endorse themselves");
        }
    }
}
