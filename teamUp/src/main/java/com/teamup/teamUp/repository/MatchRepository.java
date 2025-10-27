package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface MatchRepository extends JpaRepository<Match,UUID> {
    @Query("select m.creator.username from Match m where m.id = :id")
    Optional<String> findCreatorUsernameById(@Param("id") UUID id);
}
