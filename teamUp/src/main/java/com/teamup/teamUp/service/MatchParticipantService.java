package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.BadRequestException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.mapper.MatchParticipantMapper;
import com.teamup.teamUp.model.dto.matchParticipant.JoinRequestDto;
import com.teamup.teamUp.model.dto.matchParticipant.JoinResponseDto;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.model.entity.MatchParticipant;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.MatchParticipantStatus;
import com.teamup.teamUp.model.enums.MatchStatus;
import com.teamup.teamUp.model.id.MatchParticipantId;
import com.teamup.teamUp.repository.MatchParticipantRepository;
import com.teamup.teamUp.repository.MatchRepository;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class MatchParticipantService {
    private final MatchParticipantRepository matchParticipantRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    @Autowired
    public MatchParticipantService(MatchParticipantRepository matchParticipantRepository,  MatchRepository matchRepository, UserRepository userRepository) {
        this.matchParticipantRepository = matchParticipantRepository;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public JoinResponseDto join(UUID matchId, String authUsername, JoinRequestDto request){
        Match match = matchRepository.findByIdAndIsActiveTrue(matchId).orElseThrow(()->new NotFoundException("Match not found"));

        if(match.getStatus() == MatchStatus.CANCELED){
            throw new BadRequestException("Match not active");
        }

        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(authUsername).orElseThrow(()->new NotFoundException("User not found"));

        Instant now = Instant.now();
        if (match.getStartsAt()!=null && !now.isBefore(match.getStartsAt())){
            throw new BadRequestException("The match has already started");
        }

        if(match.getJoinDeadline()!=null && !now.isBefore(match.getJoinDeadline())){
            throw new BadRequestException("Join deadline passed");
        }

        if(matchParticipantRepository.existsById_MatchIdAndId_UserId(matchId, user.getId())){
            throw new BadRequestException("User "+user.getUsername()+" is already joined");
        }

        long participants = matchParticipantRepository.countById_MatchId(matchId);
        if(match.getMaxPlayers()!=null && match.getMaxPlayers()<=participants){
            throw new BadRequestException("Match is full");
        }

        String message = (request != null && request.message() != null)
                ? request.message().trim()
                : null;
        boolean bringsBall = request != null && Boolean.TRUE.equals(request.bringsBall());

        MatchParticipant mp = MatchParticipant.builder()
                .id(MatchParticipantId.builder()
                        .matchId(match.getId())
                        .userId(user.getId())
                        .build())
                .match(match)
                .user(user)
                .message(message)
                .bringsBall(bringsBall)
                .status(MatchParticipantStatus.REQUESTED)
                .build();

        matchParticipantRepository.save(mp);

        int after = (int) participants+1;
        return new JoinResponseDto(match.getId(),after,match.getMaxPlayers() == null ? Integer.MAX_VALUE : match.getMaxPlayers());

    }

    @Transactional
    public JoinResponseDto leave(UUID matchId, String authUsername) {
        Match match = matchRepository.findByIdAndIsActiveTrue(matchId).orElseThrow(()->new NotFoundException("Match not found"));
        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(authUsername).orElseThrow(()->new NotFoundException("User not found"));

        if(match.getCreator() != null && match.getCreator().getId().equals(user.getId())){
            throw new BadRequestException("Creator can not leave the match");
        }

        int deleted = matchParticipantRepository.deleteByMatchIdAndUserId(matchId, user.getId());
        if(deleted==0){
            throw new NotFoundException("You are not a participant");
        }

        long participants = matchParticipantRepository.countById_MatchId(matchId);
        return MatchParticipantMapper.toDto(matchId,(int)participants,match.getMaxPlayers() == null ? Integer.MAX_VALUE : match.getMaxPlayers());
    }

}
