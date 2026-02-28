package com.teamup.teamUp.websocketauth;

import com.teamup.teamUp.model.enums.MatchParticipantStatus;
import com.teamup.teamUp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Authorizes SUBSCRIBE/SEND messages for match chat.
 */
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final MatchParticipantRepository participantRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = StompHeaderAccessor.wrap(message);
        var principal = accessor.getUser();

        // Skip if user not authenticated
        if (principal == null) return null;

        // Check permissions for SUBSCRIBE and SEND
        if (accessor.getCommand() == StompCommand.SUBSCRIBE ||
                accessor.getCommand() == StompCommand.SEND) {

            String destination = accessor.getDestination(); // e.g. /topic/matches/{id}/chat
            if (destination != null && destination.contains("/matches/")) {
                UUID matchId = extractMatchId(destination);
                assertAuthorized(principal.getName(), matchId);
            }

            if (destination != null && destination.contains("/teams/")) {
                UUID teamId = extractTeamId(destination);
                assertTeamAuthorized(principal.getName(), teamId);
            }
        }

        return message;
    }

    private UUID extractMatchId(String destination) {
        String[] parts = destination.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("matches".equals(parts[i]) && i + 1 < parts.length)
                return UUID.fromString(parts[i + 1]);
        }
        throw new IllegalArgumentException("Invalid destination: " + destination);
    }

    private UUID extractTeamId(String destination) {
        String[] parts = destination.split("/");

        for (int i = 0; i < parts.length; i++) {
            if ("teams".equals(parts[i]) && i + 1 < parts.length) {
                return UUID.fromString(parts[i + 1]);
            }
        }

        throw new IllegalArgumentException("Invalid destination: " + destination);
    }

    private void assertAuthorized(String username, UUID matchId) {
        var match = matchRepository.findByIdAndIsActiveTrue(matchId).orElseThrow(() -> new RuntimeException("Match not found"));
        var user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username).orElseThrow(() -> new RuntimeException("User not found"));

        // Allow creator
        if (match.getCreator() != null && match.getCreator().getId().equals(user.getId()))
            return;

        // Allow accepted participants
        boolean ok = participantRepository.existsById_MatchIdAndId_UserIdAndStatus(matchId, user.getId(), MatchParticipantStatus.ACCEPTED);

        if (!ok)
            throw new RuntimeException("Access denied for match chat");
    }

    private void assertTeamAuthorized(String username, UUID teamId) {

        var team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));
        var user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username).orElseThrow(() -> new RuntimeException("User not found"));

        if (team.getCaptain() != null && team.getCaptain().getId().equals(user.getId()))
            return;

        boolean ok = teamMemberRepository.existsByTeamIdAndUserId(teamId, user.getId());

        if (!ok) throw new RuntimeException("Access denied for team chat");
    }
}
