package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.Match;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface MatchRepository {
    Optional<Match> findById(UUID id);
}
