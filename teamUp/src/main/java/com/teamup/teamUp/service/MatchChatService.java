package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.ForbiddenException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.mapper.MatchChatMapper;
import com.teamup.teamUp.model.dto.chat.MessageResponseDto;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.model.entity.MatchChatMessage;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.MatchParticipantStatus;
import com.teamup.teamUp.repository.MatchChatRepository;
import com.teamup.teamUp.repository.MatchParticipantRepository;
import com.teamup.teamUp.repository.MatchRepository;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class MatchChatService {
    private final MatchChatRepository  matchChatRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final MatchParticipantRepository matchParticipantRepository;

    @Autowired
    public MatchChatService(MatchChatRepository matchChatRepository, MatchRepository matchRepository, UserRepository userRepository, MatchParticipantRepository matchParticipantRepository) {
        this.matchChatRepository = matchChatRepository;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.matchParticipantRepository = matchParticipantRepository;
    }

    private void assertCanReadAndWrite(UUID matchId, UUID userId){
        Match match = matchRepository.findByIdAndIsActiveTrue(matchId).orElseThrow(()->new NotFoundException("Match not found"));

        if(match.getCreator()!=null && match.getCreator().getId().equals(userId)) return;

        boolean ok = matchParticipantRepository.existsById_MatchIdAndId_UserIdAndStatus(matchId,userId, MatchParticipantStatus.ACCEPTED);
        if(!ok){
            throw new ForbiddenException("You must be a participant in this match for using the chat");
        }
    }

    @Transactional
    public MessageResponseDto send(UUID matchID, String authUsername, String content){
        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(authUsername).orElseThrow(()->new NotFoundException("User not found"));

        assertCanReadAndWrite(matchID,user.getId());

        Match match = matchRepository.findByIdAndIsActiveTrue(matchID).orElseThrow(()->new NotFoundException("Match not found"));

        MatchChatMessage message = MatchChatMessage.builder()
                .match(match)
                .content(content.trim())
                .sender(user)
                .build();
        matchChatRepository.save(message);
        return MatchChatMapper.toDto(message);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponseDto> list(UUID matchId, String authUsername, Instant after, Pageable pageable) {
        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(authUsername).orElseThrow(()->new NotFoundException("User not found"));
        assertCanReadAndWrite(matchId,user.getId());
        return matchChatRepository.findByMatchIdAfter(matchId, after,pageable).map(MatchChatMapper::toDto);
    }

}
