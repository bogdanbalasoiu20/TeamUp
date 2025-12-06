package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.BadRequestException;
import com.teamup.teamUp.exceptions.ForbiddenException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.mapper.MatchParticipantMapper;
import com.teamup.teamUp.model.dto.matchParticipant.JoinRequestDto;
import com.teamup.teamUp.model.dto.matchParticipant.JoinResponseDto;
import com.teamup.teamUp.model.dto.matchParticipant.JoinWithStatusResponseDto;
import com.teamup.teamUp.model.dto.matchParticipant.ParticipantDto;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.model.entity.MatchParticipant;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.enums.MatchParticipantStatus;
import com.teamup.teamUp.model.enums.MatchStatus;
import com.teamup.teamUp.model.id.MatchParticipantId;
import com.teamup.teamUp.repository.MatchParticipantRepository;
import com.teamup.teamUp.repository.MatchRepository;
import com.teamup.teamUp.repository.UserRepository;
import io.micrometer.common.lang.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
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
    public JoinWithStatusResponseDto join(UUID matchId, String authUsername, JoinRequestDto request) {

        Match match = matchRepository.findByIdAndIsActiveTrue(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found"));

        if (match.getStatus() == MatchStatus.CANCELED) {
            throw new BadRequestException("Match not active");
        }

        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(authUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Instant now = Instant.now();
        if (match.getStartsAt() != null && !now.isBefore(match.getStartsAt())) {
            throw new BadRequestException("The match has already started");
        }

        if (match.getJoinDeadline() != null && !now.isBefore(match.getJoinDeadline())) {
            throw new BadRequestException("Join deadline passed");
        }

        if (matchParticipantRepository.existsById_MatchIdAndId_UserId(matchId, user.getId())) {
            throw new BadRequestException("User " + user.getUsername() + " is already joined");
        }


        long acceptedPlayers = matchParticipantRepository.countById_MatchIdAndStatus(
                matchId,
                MatchParticipantStatus.ACCEPTED
        );


        boolean full = match.getMaxPlayers() != null && acceptedPlayers >= match.getMaxPlayers();


        MatchParticipantStatus status = full
                ? MatchParticipantStatus.WAITLIST
                : MatchParticipantStatus.REQUESTED;

        String message = (request != null && request.message() != null)
                ? request.message().trim()
                : null;

        boolean bringsBall = request != null && Boolean.TRUE.equals(request.bringsBall());

        MatchParticipant mp = MatchParticipant.builder()
                .id(new MatchParticipantId(match.getId(), user.getId()))
                .match(match)
                .user(user)
                .message(message)
                .bringsBall(bringsBall)
                .status(status)
                .build();

        matchParticipantRepository.save(mp);

        long totalParticipants = matchParticipantRepository.countById_MatchId(matchId);

        return new JoinWithStatusResponseDto(
                match.getId(),
                (int) totalParticipants,
                match.getMaxPlayers(),
                status == MatchParticipantStatus.WAITLIST
        );
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

    @Transactional
    public JoinResponseDto approve(UUID matchId, UUID userId, String approverUsername) {
        Match match = matchRepository.findByIdAndIsActiveTrue(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found"));

        User approver = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(approverUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (match.getCreator() == null || !match.getCreator().getId().equals(approver.getId())) {
            throw new ForbiddenException("Only the match creator can approve requests");
        }

        Instant now = Instant.now();
        if (match.getStartsAt()!=null && !now.isBefore(match.getStartsAt())) {
            throw new BadRequestException("The match has already started");
        }
        if (match.getJoinDeadline()!=null && !now.isBefore(match.getJoinDeadline())) {
            throw new BadRequestException("Join deadline passed");
        }

        MatchParticipant mp = matchParticipantRepository
                .findById_MatchIdAndId_UserId(matchId, userId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (mp.getStatus() == MatchParticipantStatus.ACCEPTED) {
            int approved = (int) matchParticipantRepository.countById_MatchIdAndStatus(matchId, MatchParticipantStatus.ACCEPTED);
            int cap = match.getMaxPlayers() == null ? Integer.MAX_VALUE : match.getMaxPlayers();
            return MatchParticipantMapper.toDto(match.getId(),approved,cap);
        }

        long approvedNow = matchParticipantRepository.countById_MatchIdAndStatus(
                matchId, MatchParticipantStatus.ACCEPTED
        );

        if (match.getMaxPlayers()!=null && approvedNow >= match.getMaxPlayers()) {
            throw new BadRequestException("Match is full");
        }

        mp.setStatus(MatchParticipantStatus.ACCEPTED);
        matchParticipantRepository.save(mp);

        long approvedAfter = approvedNow + 1;
        int cap = match.getMaxPlayers() == null ? Integer.MAX_VALUE : match.getMaxPlayers();

        // daca meciul devine full → mutam userii REQUESTED ramasi în WAITLIST
        if (match.getMaxPlayers() != null && approvedAfter >= match.getMaxPlayers()) {

            List<MatchParticipant> remainingRequests =
                    matchParticipantRepository.findAllById_MatchIdAndStatus(
                            matchId, MatchParticipantStatus.REQUESTED);

            for (MatchParticipant pending : remainingRequests) {
                pending.setStatus(MatchParticipantStatus.WAITLIST);
            }

            matchParticipantRepository.saveAll(remainingRequests);
        }

        return MatchParticipantMapper.toDto(match.getId(), (int) approvedAfter, cap);
    }


    @Transactional
    public JoinResponseDto reject(UUID matchId, UUID userId, String moderatorUsername) {
        Match match = matchRepository.findByIdAndIsActiveTrue(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found"));

        User moderator = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(moderatorUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // doar creatorul
        if (match.getCreator() == null || !match.getCreator().getId().equals(moderator.getId())) {
            throw new ForbiddenException("Only the match creator can reject requests");
        }

        Instant now = Instant.now();
        if (match.getStartsAt()!=null && !now.isBefore(match.getStartsAt())) {
            throw new BadRequestException("The match has already started");
        }

        MatchParticipant mp = matchParticipantRepository
                .findById_MatchIdAndId_UserId(matchId, userId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        // idempotent
        if (mp.getStatus() == MatchParticipantStatus.DECLINED) {
            int approved = (int) matchParticipantRepository
                    .countById_MatchIdAndStatus(matchId, MatchParticipantStatus.ACCEPTED);
            int cap = match.getMaxPlayers()==null ? Integer.MAX_VALUE : match.getMaxPlayers();
            return MatchParticipantMapper.toDto(match.getId(),approved,cap);
        }

        // pentru cineva deja APPROVED -> folosește kick
        if (mp.getStatus() == MatchParticipantStatus.ACCEPTED) {
            throw new BadRequestException("Participant already approved. Use kick instead.");
        }

        mp.setStatus(MatchParticipantStatus.DECLINED);
        matchParticipantRepository.save(mp);

        int approvedAfter = (int) matchParticipantRepository
                .countById_MatchIdAndStatus(matchId, MatchParticipantStatus.ACCEPTED);
        int cap = match.getMaxPlayers()==null ? Integer.MAX_VALUE : match.getMaxPlayers();
        return MatchParticipantMapper.toDto(match.getId(),approvedAfter,cap);
    }


    @Transactional
    public JoinResponseDto invite(UUID matchId, UUID targetUserId,String organizerUsername){
        Match match = matchRepository.findByIdAndIsActiveTrue(matchId).orElseThrow(() -> new NotFoundException("Match not found"));
        User organizer = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(organizerUsername).orElseThrow(() -> new NotFoundException("Match organizer not found"));

        if(match.getCreator()==null || !match.getCreator().getId().equals(organizer.getId())) {
            throw new ForbiddenException("Only the match creator can invite players");
        }

        if(match.getStartsAt()!=null && match.getStartsAt().isBefore(Instant.now())) {
            throw new BadRequestException("The match has already started");
        }

        User target = userRepository.findById(targetUserId).orElseThrow(() -> new NotFoundException("Target user not found"));

        MatchParticipant mp = matchParticipantRepository.findById_MatchIdAndId_UserId(matchId,targetUserId).orElse(null);

        if(mp==null){
            mp = MatchParticipant.builder()
                    .id(MatchParticipantId.builder()
                            .matchId(match.getId())
                            .userId(target.getId())
                            .build())
                    .match(match)
                    .user(target)
                    .status(MatchParticipantStatus.INVITED)
                    .build();
        }else {
            //daca e deja ACCEPTED -> ok; daca e REQUESTED -> promovez la INVITED sau las cum e
            if (mp.getStatus() == MatchParticipantStatus.ACCEPTED) {
                int approved = (int) matchParticipantRepository.countById_MatchIdAndStatus(matchId, MatchParticipantStatus.ACCEPTED);
                int cap = match.getMaxPlayers()==null ? Integer.MAX_VALUE : match.getMaxPlayers();
                return MatchParticipantMapper.toDto(match.getId(),approved,cap);
            }
            mp.setStatus(MatchParticipantStatus.INVITED);
        }

        matchParticipantRepository.save(mp);

        int approved = (int) matchParticipantRepository.countById_MatchIdAndStatus(matchId, MatchParticipantStatus.ACCEPTED);
        int cap = match.getMaxPlayers()==null ? Integer.MAX_VALUE : match.getMaxPlayers();
        return MatchParticipantMapper.toDto(match.getId(),approved,cap);
    }


    @Transactional
    public JoinResponseDto acceptInvite(UUID matchId, String authUsername) {
        Match match = matchRepository.findByIdAndIsActiveTrue(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found"));

        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(authUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Instant now = Instant.now();
        if (match.getStartsAt()!=null && !now.isBefore(match.getStartsAt())) {
            throw new BadRequestException("The match has already started");
        }
        if (match.getJoinDeadline()!=null && !now.isBefore(match.getJoinDeadline())) {
            throw new BadRequestException("Join deadline passed");
        }

        MatchParticipant mp = matchParticipantRepository
                .findById_MatchIdAndId_UserId(matchId, user.getId())
                .orElseThrow(() -> new NotFoundException("Invitation or request not found"));

        if (mp.getStatus() == MatchParticipantStatus.ACCEPTED) {
            int approved = (int) matchParticipantRepository.countById_MatchIdAndStatus(matchId, MatchParticipantStatus.ACCEPTED);
            int cap = match.getMaxPlayers()==null ? Integer.MAX_VALUE : match.getMaxPlayers();
            return MatchParticipantMapper.toDto(match.getId(),approved,cap);
        }

        if (mp.getStatus() != MatchParticipantStatus.INVITED && mp.getStatus() != MatchParticipantStatus.REQUESTED) {
            throw new BadRequestException("You have no pending invitation to accept");
        }

        long approvedNow = matchParticipantRepository.countById_MatchIdAndStatus(matchId, MatchParticipantStatus.ACCEPTED);
        if (match.getMaxPlayers()!=null && approvedNow >= match.getMaxPlayers()) {
            // pun userul pe WAITLIST
            mp.setStatus(MatchParticipantStatus.WAITLIST);
            matchParticipantRepository.save(mp);
            throw new BadRequestException("Match is full. You were added to the waitlist");
        }

        mp.setStatus(MatchParticipantStatus.ACCEPTED);
        matchParticipantRepository.save(mp);

        int after = (int) (approvedNow + 1);
        int cap = match.getMaxPlayers()==null ? Integer.MAX_VALUE : match.getMaxPlayers();
        return MatchParticipantMapper.toDto(match.getId(),after,cap);
    }


    @Transactional
    public JoinResponseDto declineInvite(UUID matchId, String authUsername) {
        Match match = matchRepository.findByIdAndIsActiveTrue(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found"));

        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(authUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        MatchParticipant mp = matchParticipantRepository
                .findById_MatchIdAndId_UserId(matchId, user.getId())
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        if (mp.getStatus() == MatchParticipantStatus.DECLINED|| mp.getStatus()==MatchParticipantStatus.LEFT) {
            int approved = (int) matchParticipantRepository.countById_MatchIdAndStatus(matchId, MatchParticipantStatus.ACCEPTED);
            int cap = match.getMaxPlayers()==null ? Integer.MAX_VALUE : match.getMaxPlayers();
            return MatchParticipantMapper.toDto(match.getId(),approved,cap);
        }

        if (mp.getStatus() == MatchParticipantStatus.ACCEPTED) {
            throw new BadRequestException("You are already approved. Use leave instead.");
        }

        mp.setStatus(MatchParticipantStatus.DECLINED);
        matchParticipantRepository.save(mp);

        int approved = (int) matchParticipantRepository.countById_MatchIdAndStatus(matchId, MatchParticipantStatus.ACCEPTED);
        int cap = match.getMaxPlayers()==null ? Integer.MAX_VALUE : match.getMaxPlayers();
        return MatchParticipantMapper.toDto(match.getId(),approved,cap);
    }


    @Transactional(readOnly = true)
    public Page<ParticipantDto> listByStatus(UUID matchId, @Nullable MatchParticipantStatus status, Pageable pageable) {
        Match match = matchRepository.findByIdAndIsActiveTrue(matchId).orElseThrow(() -> new NotFoundException("Match not found"));

        Pageable p = pageable;
        if (pageable.getSort().isUnsorted()) {
            p = PageRequest.of(pageable.getPageNumber(),
                    Math.min(pageable.getPageSize(), 100),
                    Sort.by(Sort.Direction.ASC, "createdAt"));
        }

        return matchParticipantRepository.findParticipants(matchId, status, p);
    }


    @Transactional
    public JoinResponseDto kick(UUID matchId, UUID userId, String actorUsername) {
        Match match = matchRepository.findByIdAndIsActiveTrue(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found"));

        User actor = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(actorUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (match.getCreator() == null || !match.getCreator().getId().equals(actor.getId())) {
            throw new ForbiddenException("Only the match creator can kick participants");
        }

        MatchParticipant mp = matchParticipantRepository
                .findById_MatchIdAndId_UserId(matchId, userId)
                .orElseThrow(() -> new NotFoundException("Participant not found"));

        if (mp.getStatus() != MatchParticipantStatus.ACCEPTED) {
            if (mp.getStatus() == MatchParticipantStatus.KICKED || mp.getStatus() == MatchParticipantStatus.LEFT) {
                int approved = (int) matchParticipantRepository.countById_MatchIdAndStatus(matchId, MatchParticipantStatus.ACCEPTED);
                int cap = match.getMaxPlayers()==null ? Integer.MAX_VALUE : match.getMaxPlayers();
                return MatchParticipantMapper.toDto(match.getId(),approved,cap);
            }
            throw new BadRequestException("User is not approved; use reject");
        }

        mp.setStatus(MatchParticipantStatus.KICKED);
        matchParticipantRepository.save(mp);

        int approvedAfter = (int) matchParticipantRepository.countById_MatchIdAndStatus(matchId, MatchParticipantStatus.ACCEPTED);
        int cap = match.getMaxPlayers()==null ? Integer.MAX_VALUE : match.getMaxPlayers();
        return MatchParticipantMapper.toDto(match.getId(),approvedAfter,cap);
    }


    @Transactional
    public ParticipantDto promoteFromWaitlist(UUID matchId, UUID userId, String adminUsername) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found"));

        // doar creatorul poate promova
        if (!match.getCreator().getUsername().equals(adminUsername)) {
            throw new ForbiddenException("Only the match creator can manage waitlist");
        }

        MatchParticipant participant = matchParticipantRepository
                .findById(new MatchParticipantId(matchId, userId))
                .orElseThrow(() -> new NotFoundException("User not found in match"));

        if (participant.getStatus() != MatchParticipantStatus.WAITLIST) {
            throw new BadRequestException("User is not on the waitlist");
        }

        long acceptedCount = matchParticipantRepository.countById_MatchIdAndStatus(
                matchId,
                MatchParticipantStatus.ACCEPTED
        );

        if (match.getMaxPlayers() != null && acceptedCount >= match.getMaxPlayers()) {
            throw new BadRequestException("Match is full");
        }

        participant.setStatus(MatchParticipantStatus.ACCEPTED);
        matchParticipantRepository.save(participant);

        return new ParticipantDto(
                participant.getUser().getId(),
                participant.getUser().getUsername(),
                participant.getStatus(),
                participant.getBringsBall(),
                participant.getCreatedAt()
        );
    }

}
