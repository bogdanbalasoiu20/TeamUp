package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
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

    @Query("""
        select v from Venue v
        where v.isActive = true
        and v.latitude between :minLat and :maxLat
        and v.longitude between  :minLng and :maxLng
    """)
    List<Venue> findInBBox (@Param("minLat") double minLat,
                           @Param("maxLat") double maxLat,
                           @Param("minLng") double minLng,
                           @Param("maxLng") double maxLng);

    Optional<Venue> findByOsmTypeAndOsmId(String osmType, Long osmId);


    @Query(value = """
      select v.*
      from venues v
      where v.is_active = true
        and v.geom is not null
        and st_dwithin(
              v.geom::geography,
              st_setsrid(st_makepoint(:lng, :lat), 4326)::geography,
              :radiusmeters
            )
      order by st_distancesphere(
              v.geom,
              st_setsrid(st_makepoint(:lng, :lat), 4326)
            )
      limit :limit
""", nativeQuery = true)
    List<Venue> findNearbyOrdered(@Param("lat") double lat,
                                  @Param("lng") double lng,
                                  @Param("radiusMeters") double radiusMeters,
                                  @Param("limit") int limit);


    @Query(value = """
        select v.* from venues v 
        where v.is_active = true
        and v.geom is not null
        and v.geom && ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326)
        order by v.name asc
        limit :limit
""",nativeQuery = true)
    List<Venue> findInBBoxPostgis(@Param("minLat") double minLat,
                                  @Param("minLng") double minLng,
                                  @Param("maxLat") double maxLat,
                                  @Param("maxLng") double maxLng,
                                  @Param("limit") int limit);

}
