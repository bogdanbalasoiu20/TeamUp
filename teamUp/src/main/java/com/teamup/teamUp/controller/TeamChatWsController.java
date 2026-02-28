package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.chat.MessageRequestDto;
import com.teamup.teamUp.service.TeamChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TeamChatWsController {
    private final TeamChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/teams/{teamId}/chat.send")
    public void send(@DestinationVariable UUID teamId, MessageRequestDto req, Principal principal) {
        var dto = chatService.send(teamId, principal.getName(), req.content());
        messagingTemplate.convertAndSend("/topic/teams/" + teamId + "/chat", dto);
    }
}