package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.dto.match.MatchMapPinDto;
import com.teamup.teamUp.model.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public interface MatchRepository extends JpaRepository<Match,UUID> {
    @EntityGraph(attributePaths = {"creator", "venue","venue.city"})
    Optional<Match> findByIdAndIsActiveTrue(UUID id);

    @Query("select m.creator.username from Match m where m.id = :id")
    Optional<String> findCreatorUsernameById(@Param("id") UUID id);

    @Query(value = """
      select m from Match m
      join m.venue v
      left join v.city c
      where m.isActive = true
        and (:city is null or lower(c.slug) = lower(:city))
        and (:dateFrom is null or m.startsAt >= :dateFrom)
        and (:dateTo   is null or m.startsAt <  :dateTo)
      order by m.startsAt asc
    """,
                countQuery = """
      select count(m) from Match m
      join m.venue v
      left join v.city c
      where m.isActive = true
        and (:city is null or lower(c.slug) = lower(:city))
        and (:dateFrom is null or m.startsAt >= :dateFrom)
        and (:dateTo   is null or m.startsAt <  :dateTo)
""")
    Page<Match> searchByCityAndDate(String city, Instant dateFrom, Instant dateTo, Pageable pageable);



    @Query("""
select new com.teamup.teamUp.model.dto.match.MatchMapPinDto(
     m.id, v.latitude, v.longitude, m.title, m.startsAt, m.currentPlayers, m.maxPlayers, v.name
  )
  from Match m
  join m.venue v
  where m.isActive = true
    and v.latitude  between :minLat and :maxLat
    and v.longitude between :minLng and :maxLng
    and m.startsAt >= :dateFrom
    and m.startsAt <  :dateTo
  order by m.startsAt asc
""")
    List<MatchMapPinDto> findPinsInBBOx(
            double minLat,
            double minLng,
            double maxLat,
            double maxLng,
            Instant dateFrom,
            Instant dateTo,
            Pageable pageable
    );

}
