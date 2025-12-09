package com.teamup.teamUp.events;

import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.model.entity.MatchParticipant;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.NotificationType;
import com.teamup.teamUp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationEvents {
    private final NotificationService notificationService;

    public void friendRequestReceived(User sender, User receiver) {
        notificationService.send(receiver, NotificationType.FRIEND_REQUEST_RECEIVED, "Friend Request", sender.getUsername() + " sent you a friend request", Map.of("senderId", sender.getId()));
    }

    public void friendRequestAccepted(User requester, User accepter) {
        notificationService.send(requester, NotificationType.FRIEND_REQUEST_ACCEPTED, "Friend Request Accepted", accepter.getUsername() + " accepted your friend request", Map.of("friendId", accepter.getId()));
    }

    public void matchInviteReceived(User inviter, User invited, Match match) {
        notificationService.send(
                invited,
                NotificationType.MATCH_INVITE_RECEIVED,
                "Match Invitation",
                inviter.getUsername() + " invited you to a match",
                Map.of("matchId", match.getId(), "inviterId", inviter.getId())
        );
    }

    public void matchInviteAccepted(User invited, User inviter, Match match) {
        notificationService.send(
                inviter,
                NotificationType.MATCH_INVITE_ACCEPTED,
                "Match invitation accepted",
                invited.getUsername() + " accepted your match invitation",
                Map.of("matchId", match.getId(), "userId", invited.getId()
                )
        );
    }


    public void matchUpdated(Match match, List<MatchParticipant> receivers) {
        Instant now = Instant.now();

        for (MatchParticipant receiver : receivers) {
            if (receiver.getUser().getId().equals(match.getCreator().getId())) {
                continue;
            }

            notificationService.send(
                    receiver.getUser(),
                    NotificationType.MATCH_UPDATED,
                    "Match updated",
                    "A match you joined has been updated",
                    Map.of("matchId", match.getId(), "updatedAt", now
                    )
            );
        }
    }


    public void matchCancelled(Match match, User receiver) {
        notificationService.send(
                receiver,
                NotificationType.MATCH_CANCELLED,
                "Match cancelled",
                "A match you joined has been cancelled",
                Map.of("matchId", match.getId())
        );
    }

    public void matchStartingSoon(Match match, User receiver) {
        notificationService.send(
                receiver,
                NotificationType.MATCH_STARTING_SOON,
                "Match starting soon",
                "Your match starts in less than 2 hours",
                Map.of("matchId", match.getId())
        );
    }
}
