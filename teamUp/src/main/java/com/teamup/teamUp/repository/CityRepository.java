package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {
    Optional<City> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
