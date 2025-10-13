package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {
}
