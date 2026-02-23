package com.teamup.teamUp.model.entity;

import com.teamup.teamUp.model.enums.SquadType;
import com.teamup.teamUp.model.enums.TeamRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "team_members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"team_id", "user_id"}),
                @UniqueConstraint(columnNames = {"team_id", "squad_type", "slot_index"})
        })
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TeamRole role = TeamRole.PLAYER;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private SquadType squadType = SquadType.BENCH;

    @Builder.Default
    private Integer slotIndex = 0;

    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

}
