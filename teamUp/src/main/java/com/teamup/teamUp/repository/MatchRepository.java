package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface MatchRepository extends JpaRepository<Match,UUID> {

}
