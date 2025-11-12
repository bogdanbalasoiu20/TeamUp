package com.teamup.teamUp.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@Table(name = "user_stats")
@Immutable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsView {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    private Double avgPace;
    private Double avgShooting;
    private Double avgPassing;
    private Double avgDefending;
    private Double avgDribbling;
    private Double avgPhysical;
    private Integer totalRatings;
}
