package com.teamup.teamUp.model.entity;


import com.teamup.teamUp.model.enums.VenueSource;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "venues")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Venue {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String city;
    private Double latitude;
    private Double longitude;

    @Column(name = "osm_type",length = 16)
    private String osmType;

    @Column(name = "osm_id")
    private Long osmId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags_json", columnDefinition = "jsonb")
    private Map<String, Object> tagsJson;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private VenueSource source = VenueSource.OSM;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false,updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    private void checkLatitudeAndLongitude() {
        if(latitude !=null && (latitude >90 || latitude <-90))
            throw new IllegalArgumentException("Latitude must be between -90 and 90");

        if(longitude != null && (longitude >180 || longitude <-180))
            throw new IllegalArgumentException("Longitude must be between -180 and 180");

    }
}
