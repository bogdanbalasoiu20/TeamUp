package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.chat.MessageRequestDto;
import com.teamup.teamUp.service.MatchChatService;
import com.teamup.teamUp.mapper.MatchChatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * Handles WebSocket chat messages for each match.
 */
@Controller
@RequiredArgsConstructor
public class MatchChatWsController {

    private final MatchChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MatchChatMapper matchChatMapper;

    // Client SEND → /app/matches/{matchId}/chat.send
    @MessageMapping("/matches/{matchId}/chat.send")
    public void send(@DestinationVariable UUID matchId, MessageRequestDto req, Principal principal) {
        // Save message (returns DTO)
        var dto = chatService.send(matchId, principal.getName(), req.content());

        // Broadcast DTO directly
        messagingTemplate.convertAndSend("/topic/matches/" + matchId + "/chat", dto);
    }
}
