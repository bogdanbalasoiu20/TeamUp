package com.teamup.teamUp.model.entity;

import com.teamup.teamUp.model.enums.FriendRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "friend_requests")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"requester", "addressee"})
public class FriendRequest {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fr_requester"))
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "addressee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fr_addressee"))
    private User addressee;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private FriendRequestStatus status =  FriendRequestStatus.PENDING;

    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    private void validate(){
        if(requester != null && addressee != null && requester.getId().equals(addressee.getId())){
            throw new IllegalArgumentException("requester and addressee can't be the same");
        }
        if(status == null){
            status = FriendRequestStatus.PENDING;
        }
        if(status != FriendRequestStatus.PENDING && respondedAt == null){
            respondedAt = Instant.now();
        }
    }

}
