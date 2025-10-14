package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;


public interface VenueRepository extends JpaRepository<Venue, UUID> {
    @Query("""
      SELECT v FROM Venue v
      WHERE (:city IS NULL OR LOWER(v.city) = LOWER(:city))
        AND (:q IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :q, '%')))
        AND (:activeOnly = FALSE OR v.isActive = TRUE)
      ORDER BY v.name ASC
    """)
    Page<Venue> search(@Param("city") String city ,@Param("q") String q, @Param("activeOnly") boolean activeOnly, Pageable pageable);
}
