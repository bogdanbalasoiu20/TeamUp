package com.teamup.teamUp.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "match_chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"match", "senderParticipation"})
public class MatchChatMessage {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "match_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mcm_match"))
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumns(value={
            @JoinColumn(name = "match_id", referencedColumnName = "match_id", insertable = false, updatable = false),
            @JoinColumn(name = "sender_id", referencedColumnName = "user_id", nullable = false)
    }, foreignKey = @ForeignKey(name = "fk_mcm_sender_is_participant"))
    private MatchParticipant senderParticipation;

    @Column(nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Transient //transient ii spune lui hibernate ca metoda nu e mapata la nicio coloana din tabel si sa nu o salveze in db
    public User getSenderUser(){
        return senderParticipation!=null ? senderParticipation.getUser():null;
    }

    @PrePersist
    @PreUpdate
    private void validate(){
        if(content==null || content.isBlank()){
            throw new IllegalArgumentException("Content cannot be empty");
        }
        content = content.trim();
        if(match==null || senderParticipation == null)
            throw new IllegalArgumentException("Match or sender must be provided");

        if(!senderParticipation.getMatch().getId().equals(match.getId()))
            throw new IllegalArgumentException("Sender participant does not belong to this match");
    }
}
