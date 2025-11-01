package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.BadRequestException;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.exceptions.ResourceConflictException;
import com.teamup.teamUp.mapper.MatchMapper;
import com.teamup.teamUp.model.dto.match.MatchCreateRequestDto;
import com.teamup.teamUp.model.dto.match.MatchMapPinDto;
import com.teamup.teamUp.model.dto.match.MatchResponseDto;
import com.teamup.teamUp.model.dto.match.MatchUpdateRequestDto;
import com.teamup.teamUp.model.entity.Match;
import com.teamup.teamUp.model.entity.User;
import com.teamup.teamUp.model.entity.Venue;
import com.teamup.teamUp.model.enums.MatchStatus;
import com.teamup.teamUp.model.enums.MatchVisibility;
import com.teamup.teamUp.repository.MatchParticipantRepository;
import com.teamup.teamUp.repository.MatchRepository;
import com.teamup.teamUp.repository.UserRepository;
import com.teamup.teamUp.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class MatchService {
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final MatchMapper matchMapper;
    private final MatchParticipantRepository matchParticipantRepository;

    @Autowired
    public MatchService(MatchRepository matchRepository, UserRepository userRepository, VenueRepository venueRepository, MatchMapper matchMapper, MatchParticipantRepository matchParticipantRepository) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
        this.matchMapper = matchMapper;
        this.matchParticipantRepository = matchParticipantRepository;
    }

    @Transactional(readOnly = true)
    public Match findById(UUID id){
        return matchRepository.findByIdAndIsActiveTrue(id).orElseThrow(()-> new NotFoundException("Match not found"));
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
        return matchMapper.toDto(match);
    }

    @Transactional
    public Match update(UUID id, MatchUpdateRequestDto request){
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
        }

        if (request.startsAt() != null && !Objects.equals(match.getStartsAt(), request.startsAt())) {
            match.setStartsAt(request.startsAt());
        }

        if(request.durationMinutes()!=null && !Objects.equals(match.getDurationMinutes(), request.durationMinutes())){
            if(request.durationMinutes()<0){
                throw new BadRequestException("duration minutes can't be negative");
            }
            match.setDurationMinutes(request.durationMinutes());
        }
        if (request.maxPlayers() != null && !Objects.equals(match.getMaxPlayers(), request.maxPlayers())) {
            if (request.maxPlayers() <= 1) {
                throw new BadRequestException("Invalid max players number");
            }
            if (request.maxPlayers() < participantsNumber) {
                throw new BadRequestException("maxPlayers cannot be less than current participants (" + participantsNumber + ")");
            }
            match.setMaxPlayers(request.maxPlayers());
        }
        if (request.title() != null && !Objects.equals(match.getTitle(), request.title())) {
            match.setTitle(request.title());
        }
        if (request.notes() != null && !Objects.equals(match.getNotes(), request.notes())) {
            match.setNotes(request.notes());
        }
        if (request.visibility() != null && !Objects.equals(match.getVisibility(), request.visibility())) {
            match.setVisibility(request.visibility());
        }
        if (request.joinDeadline() != null) {
            Instant effectiveStarts = (request.startsAt() != null) ? request.startsAt() : match.getStartsAt();
            if (effectiveStarts != null && !request.joinDeadline().isBefore(effectiveStarts)) {
                throw new BadRequestException("Join deadline must be before start time");
            }
            match.setJoinDeadline(request.joinDeadline());
        }
        if (request.totalPrice() != null && !Objects.equals(match.getTotalPrice(), request.totalPrice())) {
            match.setTotalPrice(request.totalPrice());
        }

        return matchRepository.save(match);
    }

    @Transactional
    public void delete(UUID id){
        Match match = findById(id);

        if(Boolean.TRUE.equals(match.getIsActive())){
            match.setIsActive(false);
            match.setStatus(MatchStatus.CANCELED);
        }
    }

    @Transactional(readOnly = true)
    public Page<MatchResponseDto> search(String city, Instant dateFrom, Instant dateTo, Pageable pageable){
        return matchRepository.searchByCityAndDate(city, dateFrom, dateTo, pageable).map(matchMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<MatchMapPinDto> nearbyPins(double minLat, double minLng, double maxLat, double maxLng, Instant dateFrom, Instant dateTo, int limit){
        var pageable = PageRequest.of(0, Math.min(limit,500), Sort.by("startsAt").ascending());
        return matchRepository.findPinsInBBOx(minLat, minLng, maxLat, maxLng, dateFrom, dateTo, pageable);
    }
}
