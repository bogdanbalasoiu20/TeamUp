package com.teamup.teamUp.model.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "cities", indexes = {@Index(name = "ix_city_slug", columnList = "slug",unique = true)})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120,unique = true)
    private String slug; //numele orasului fara diacritice sau majuscule

    @Column(name= "min_lat")
    private Double minLat;

    @Column(name = "min_lng")
    private Double minLng;

    @Column(name = "max_lat")
    private Double maxLat;

    @Column(name = "max_lng")
    private Double maxLng;

    @Column(name = "center_lat")
    private Double centerLat;

    @Column(name = "center_lng")
    private Double centerLng;

    @Column(length = 5)
    private String countryCode;
}
