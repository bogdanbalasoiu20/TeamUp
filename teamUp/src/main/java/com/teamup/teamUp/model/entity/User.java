package com.teamup.teamUp.model.entity;


import com.teamup.teamUp.model.enums.Position;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false,unique = true)
    private String username;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(name = "password_hash",nullable = false)
    private String passwordHash;

    private LocalDate birthday;

    @Column(name = "phone_number",nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", length = 32)
    private Position position;

    private String city;

    @Column(length = 300)
    private String description;
    private String rank;

    @Column(name = "photo_url")
    private String photoUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",nullable = false)
    private Instant updatedAt;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    @Column(name = "token_version",nullable = false)
    @Builder.Default
    private Integer tokenVersion = 0;

    @Column(name = "is_deleted",nullable = false)
    @Builder.Default
    private boolean deleted=false;
}
