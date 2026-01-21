package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.Notification;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    long countByUserAndIsSeenFalse(User user);


    @Modifying
    @Query(
            value = """
        update notifications
        set is_seen = true,
            seen_at = now()
        where type = CAST(:type AS notification_type)
          and payload ->> 'matchId' = :matchId
    """,
            nativeQuery = true
    )
    void markFinishMatchNotificationSeen(
            @Param("type") String type,
            @Param("matchId") String matchId
    );

}
