package com.teamup.teamUp.controller;

import com.teamup.teamUp.model.dto.chat.MessageRequestDto;
import com.teamup.teamUp.model.dto.chat.MessageResponseDto;
import com.teamup.teamUp.service.TeamChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams/{teamId}/chat/messages")
@RequiredArgsConstructor
public class TeamChatController {

    private final TeamChatService chatService;

    @PostMapping
    public ResponseEntity<ResponseApi<MessageResponseDto>> send(@PathVariable UUID teamId, @Valid @RequestBody MessageRequestDto req, Authentication auth) {
        var message = chatService.send(teamId, auth.getName(), req.content());
        return ResponseEntity.ok(new ResponseApi<>("Message sent", message, true));
    }

    @GetMapping
    public ResponseEntity<ResponseApi<Page<MessageResponseDto>>> list(
            @PathVariable UUID teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            Authentication auth) {

        var page = chatService.list(teamId, auth.getName(), after, pageable);
        return ResponseEntity.ok(new ResponseApi<>("Messages fetched", page, true));
    }
}
