package com.teamup.teamUp.chemistry.service;

import com.teamup.teamUp.chemistry.ArchetypeDetector;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.model.enums.PlayerArchetype;
import com.teamup.teamUp.repository.MatchParticipantRepository;
import com.teamup.teamUp.repository.PlayerCardStatsRepository;
import com.teamup.teamUp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepo;
    private final PlayerCardStatsRepository cardRepo;
    private final MatchParticipantRepository matchRepo;
    private final ArchetypeDetector archetypeDetector;

    private static final int MIN_MATCHES_FOR_ROLE = 5;

    public UserRoleServiceImpl(
            UserRepository userRepo,
            PlayerCardStatsRepository cardRepo,
            MatchParticipantRepository matchRepo,
            ArchetypeDetector archetypeDetector
    ) {
        this.userRepo = userRepo;
        this.cardRepo = cardRepo;
        this.matchRepo = matchRepo;
        this.archetypeDetector = archetypeDetector;
    }

    @Override
    public PlayerArchetype getRole(UUID userId) {

        var user = userRepo.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        int matches = matchRepo.countByUser_Id(userId);

        if (matches < MIN_MATCHES_FOR_ROLE) {
            return PlayerArchetype.ROOKIE;
        }

        var stats = cardRepo.findById(userId).orElseThrow(() -> new NotFoundException("Stats not found"));

        return archetypeDetector.detect(stats, user.getPosition());
    }
}
