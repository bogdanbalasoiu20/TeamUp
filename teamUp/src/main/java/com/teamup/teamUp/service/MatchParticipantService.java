package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.BadRequestException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.dto.matchParticipant.JoinRequestDto;
import com.teamup.teamUp.model.dto.matchParticipant.JoinResponseDto;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.model.entity.MatchParticipant;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.MatchParticipantStatus;
import com.teamup.teamUp.model.enums.MatchStatus;
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

        if(matchParticipantRepository.existsByMatchIdAndUserId(matchId, user.getId())){
            throw new BadRequestException("User "+user.getUsername()+" is already joined");
        }

        long participants = matchParticipantRepository.countByMatchId(matchId);
        if(match.getMaxPlayers()!=null && match.getMaxPlayers()<=participants){
            throw new BadRequestException("Match is full");
        }

        MatchParticipant mp = MatchParticipant.builder()
                .match(match)
                .user(user)
                .message(request.message())
                .bringsBall(request.bringsBall())
                .status(MatchParticipantStatus.REQUESTED)
                .build();

        matchParticipantRepository.save(mp);

        int after = (int) participants+1;
        return new JoinResponseDto(match.getId(),after,match.getMaxPlayers() == null ? Integer.MAX_VALUE : match.getMaxPlayers());

    }

}
