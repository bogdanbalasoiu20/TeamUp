package com.teamup.teamUp.service;

import com.teamup.teamUp.events.NotificationEvents;
import com.teamup.teamUp.exceptions.BadRequestException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.exceptions.ResourceConflictException;
import com.teamup.teamUp.mapper.MatchMapper;
import com.teamup.teamUp.model.dto.match.*;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.model.entity.MatchParticipant;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.entity.Venue;
import com.teamup.teamUp.model.enums.MatchParticipantStatus;
import com.teamup.teamUp.model.enums.MatchStatus;
import com.teamup.teamUp.model.enums.MatchVisibility;
import com.teamup.teamUp.model.enums.NotificationType;
import com.teamup.teamUp.model.id.MatchParticipantId;
import com.teamup.teamUp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class MatchService {
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final MatchMapper matchMapper;
    private final MatchParticipantRepository matchParticipantRepository;
    private final NotificationEvents notificationEvents;
    private final NotificationRepository notificationRepository;

    @Autowired
    public MatchService(MatchRepository matchRepository, UserRepository userRepository, VenueRepository venueRepository, MatchMapper matchMapper, MatchParticipantRepository matchParticipantRepository, NotificationEvents notificationEvents, NotificationRepository notificationRepository) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
        this.matchMapper = matchMapper;
        this.matchParticipantRepository = matchParticipantRepository;
        this.notificationEvents = notificationEvents;
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public Match findById(UUID id){
        return matchRepository.findByIdAndIsActiveTrue(id).orElseThrow(()-> new NotFoundException("Match not found"));
    }

    @Transactional
    public Page<MatchResponseDto> findAll(Pageable pageable) {
        return matchRepository.findAll(pageable)
                .map(matchMapper::toDto);
    }


    @Transactional
    public MatchResponseDto create(MatchCreateRequestDto request, String authUsername){
        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(authUsername).orElseThrow(() -> new NotFoundException("User not found"));
        Venue venue = venueRepository.findById(request.venueId()).orElseThrow(() -> new NotFoundException("Venue not found"));
        if(Boolean.FALSE.equals(venue.getIsActive())){
            throw new BadRequestException("Venue not active");
        }

        MatchVisibility matchVisibility = (request.visibility() == null) ? MatchVisibility.PUBLIC : request.visibility();

        var match = Match.builder()
                .creator(user)
                .venue(venue)
                .startsAt(request.startsAt())
                .durationMinutes(request.durationMinutes())
                .maxPlayers(request.maxPlayers())
                .title(request.title().trim())
                .notes(request.notes().trim())
                .visibility(matchVisibility)
                .joinDeadline(request.joinDeadline())
                .totalPrice(request.totalPrice())
                .build();

        matchRepository.save(match);

        MatchParticipant mp = MatchParticipant.builder()
                .id(new MatchParticipantId(match.getId(), user.getId()))
                .match(match)
                .user(user)
                .status(MatchParticipantStatus.ACCEPTED)
                .bringsBall(false)
                .message(null)
                .build();

        matchParticipantRepository.save(mp);
        return matchMapper.toDto(match);
    }

    @Transactional
    public Match update(UUID id, MatchUpdateRequestDto request){
        boolean isUpdated = false;
        Match match = findById(id);

        if (request.version() != null && !Objects.equals(request.version(), match.getVersion())) {
            throw new ResourceConflictException("version conflict");
        }

        long participantsNumber = matchParticipantRepository.countById_MatchId(id);

        if(request.venueId()!=null && !Objects.equals(request.venueId(), match.getVenue().getId())) {
            if(participantsNumber>1){
                throw new BadRequestException("Can not change the venue after participants have joined");
            }
            Venue venue = venueRepository.findById(request.venueId()).orElseThrow(() -> new NotFoundException("Venue not found"));
            match.setVenue(venue);
            isUpdated = true;
        }

        if (request.startsAt() != null && !Objects.equals(match.getStartsAt(), request.startsAt())) {
            match.setStartsAt(request.startsAt());
            isUpdated = true;
        }

        if(request.durationMinutes()!=null && !Objects.equals(match.getDurationMinutes(), request.durationMinutes())){
            if(request.durationMinutes()<0){
                throw new BadRequestException("duration minutes can't be negative");
            }
            match.setDurationMinutes(request.durationMinutes());
            isUpdated = true;
        }
        if (request.maxPlayers() != null && !Objects.equals(match.getMaxPlayers(), request.maxPlayers())) {
            if (request.maxPlayers() <= 1) {
                throw new BadRequestException("Invalid max players number");
            }
            if (request.maxPlayers() < participantsNumber) {
                throw new BadRequestException("maxPlayers cannot be less than current participants (" + participantsNumber + ")");
            }
            match.setMaxPlayers(request.maxPlayers());
            isUpdated = true;
        }
        if (request.title() != null && !Objects.equals(match.getTitle(), request.title())) {
            match.setTitle(request.title());
            isUpdated = true;
        }
        if (request.notes() != null && !Objects.equals(match.getNotes(), request.notes())) {
            match.setNotes(request.notes());
            isUpdated = true;
        }
        if (request.visibility() != null && !Objects.equals(match.getVisibility(), request.visibility())) {
            match.setVisibility(request.visibility());
            isUpdated = true;
        }
        if (request.joinDeadline() != null) {
            Instant effectiveStarts = (request.startsAt() != null) ? request.startsAt() : match.getStartsAt();
            if (effectiveStarts != null && !request.joinDeadline().isBefore(effectiveStarts)) {
                throw new BadRequestException("Join deadline must be before start time");
            }
            match.setJoinDeadline(request.joinDeadline());
            isUpdated = true;
        }
        if (request.totalPrice() != null && !Objects.equals(match.getTotalPrice(), request.totalPrice())) {
            match.setTotalPrice(request.totalPrice());
            isUpdated = true;
        }

        if(isUpdated){
            List<MatchParticipant> participants = matchParticipantRepository.findAllById_MatchIdAndStatus(id,MatchParticipantStatus.ACCEPTED);
            notificationEvents.matchUpdated(match,participants);
        }

        return matchRepository.save(match);
    }

    @Transactional
    public void delete(UUID id){
        Match match = findById(id);

        if(Boolean.TRUE.equals(match.getIsActive())){
            match.setIsActive(false);
            match.setStatus(MatchStatus.CANCELED);
            matchRepository.save(match);
        }
    }

    @Transactional(readOnly = true)
    public Page<MatchResponseDto> search(String city, Instant dateFrom, Instant dateTo, Pageable pageable){
        return matchRepository.searchByCityAndDate(city, dateFrom, dateTo, pageable).map(matchMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<MatchMapPinDto> nearbyPins(double minLat, double minLng, double maxLat, double maxLng,
                                           Instant dateFrom, Instant dateTo, int limit) {

        if (dateFrom == null) dateFrom = Instant.EPOCH;
        if (dateTo == null) dateTo = Instant.parse("2100-01-01T00:00:00Z");

        Pageable pageable = PageRequest.of(0, Math.min(limit, 500),
                Sort.by("startsAt").ascending());

        return matchRepository.findPinsInBBOx(
                minLat, minLng, maxLat, maxLng,
                dateFrom, dateTo,
                pageable
        );
    }


    @Transactional
    public void finishMatch(UUID matchId, String authUsername) {

        Match match = findById(matchId);

        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(authUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!match.getCreator().getId().equals(user.getId())) {
            throw new BadRequestException("You are not allowed to finish this match");
        }

        if (match.getStatus() == MatchStatus.DONE) {
            throw new BadRequestException("Match already finished");
        }

        match.setStatus(MatchStatus.DONE);
        match.setRatingOpenedAt(Instant.now());
        match.setRatingsFinalized(false);
        matchRepository.save(match);

        notificationRepository.markFinishMatchNotificationSeen(
                NotificationType.MATCH_FINISH_CONFIRMATION.name(),
                matchId.toString()
        );


        List<MatchParticipant> participants =
                matchParticipantRepository.findAllById_MatchIdAndStatus(
                        matchId,
                        MatchParticipantStatus.ACCEPTED
                );

        notificationEvents.matchFinished(match, participants);
    }


    @Transactional(readOnly = true)
    public FinishPendingMatchDto getOldestFinishPendingMatch(String username) {

        User user = userRepository
                .findByUsernameIgnoreCaseAndDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Instant now = Instant.now();

        return matchRepository
                .findAllByCreatorAndStatusNotOrderByStartsAtAsc(user, MatchStatus.DONE)
                .stream()
                .filter(m ->
                        now.isAfter(
                                m.getStartsAt().plus(m.getDurationMinutes(), ChronoUnit.MINUTES)
                        )
                )
                .findFirst()
                .map(m -> new FinishPendingMatchDto(
                        m.getId(),
                        m.getStartsAt(),
                        m.getDurationMinutes()
                ))
                .orElse(null);
    }



}
