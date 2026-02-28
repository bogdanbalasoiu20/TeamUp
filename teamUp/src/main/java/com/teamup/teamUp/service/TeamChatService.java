package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.ForbiddenException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.dto.chat.MessageResponseDto;
import com.teamup.teamUp.model.entity.Team;
import com.teamup.teamUp.model.entity.TeamChatMessage;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.repository.TeamChatRepository;
import com.teamup.teamUp.repository.TeamMemberRepository;
import com.teamup.teamUp.repository.TeamRepository;
import com.teamup.teamUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamChatService {

    private final TeamChatRepository teamChatRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    private void assertCanReadAndWrite(UUID teamId, UUID userId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new NotFoundException("Team not found"));

        // Captain allowed
        if (team.getCaptain() != null && team.getCaptain().getId().equals(userId)) return;

        boolean isMember = teamMemberRepository.existsByTeamIdAndUserId(teamId, userId);

        if (!isMember) throw new ForbiddenException("You must be a team member to use the team chat");
    }

    @Transactional
    public MessageResponseDto send(UUID teamId, String username, String content) {

        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username).orElseThrow(() -> new NotFoundException("User not found"));

        assertCanReadAndWrite(teamId, user.getId());

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new NotFoundException("Team not found"));

        TeamChatMessage message = TeamChatMessage.builder()
                .team(team)
                .sender(user)
                .content(content.trim())
                .build();

        teamChatRepository.save(message);

        return new MessageResponseDto(
                message.getId(),
                teamId,
                user.getId(),
                user.getUsername(),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<MessageResponseDto> list(UUID teamId, String username, Instant after, Pageable pageable) {

        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username).orElseThrow(() -> new NotFoundException("User not found"));

        assertCanReadAndWrite(teamId, user.getId());

        Page<TeamChatMessage> page;

        if (after == null) {
            page = teamChatRepository.findInitialMessages(teamId, pageable);
        } else {
            page = teamChatRepository.findByTeamIdAndCreatedAtAfter(teamId, after, pageable);
        }

        return page.map(m ->
                new MessageResponseDto(
                        m.getId(),
                        teamId,
                        m.getSender().getId(),
                        m.getSender().getUsername(),
                        m.getContent(),
                        m.getCreatedAt()
                )
        );
    }
}