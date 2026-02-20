package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    Optional<Team> findByName(String name);
    boolean existsByName(String name);


    @Query("""
    SELECT t FROM Team t
    WHERE t.id NOT IN (
        SELECT tm.team.id FROM TeamMember tm
        WHERE tm.user.username = :username
    )
    AND (:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))
""")
    Page<Team> exploreTeams(@Param("username") String username, @Param("search") String search, Pageable pageable);
}
