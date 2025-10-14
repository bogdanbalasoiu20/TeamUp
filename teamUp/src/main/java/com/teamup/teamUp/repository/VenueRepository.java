package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {
    Page<Venue> search(@Param("city") String city ,@Param("q") String q, @Param("activeOnly") boolean activeOnly, Pageable pageable);
}
