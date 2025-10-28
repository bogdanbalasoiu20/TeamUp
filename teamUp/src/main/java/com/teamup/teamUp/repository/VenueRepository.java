package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface VenueRepository extends JpaRepository<Venue, UUID> {
    @EntityGraph(attributePaths = "city")
    @Query("""
      SELECT v FROM Venue v
      LEFT JOIN v.city c
      WHERE (:city IS NULL OR c.slug = LOWER(:city))
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
    and ST_DWithin(
          v.geom::geography,
          ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
          :radiusMeters
        )
  order by ST_Distance(
          v.geom::geography,
          ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
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
    and COALESCE(v.area_geom, v.geom) is not null
    and COALESCE(v.area_geom, v.geom) && ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326)
  order by v.name asc
  limit :limit
""", nativeQuery = true)
    List<Venue> findInBBoxPostgis(@Param("minLat") double minLat,
                                  @Param("minLng") double minLng,
                                  @Param("maxLat") double maxLat,
                                  @Param("maxLng") double maxLng,
                                  @Param("limit") int limit);


    @Query(value = """
      select v.*
      from venues v
      left join cities c on c.id = v.city_id
      where v.is_active = true
        and (
             v.name ilike concat('%', :q, '%')
             or (c.name is not null and c.name ilike concat('%', :q, '%'))
        )
      order by
        case when :cityHint is not null and c.slug = lower(:cityHint) then 1 else 0 end desc,
        greatest(similarity(v.name, :q), similarity(coalesce(c.name,''), :q)) desc,
        v.name asc
      limit :limit
""", nativeQuery = true)
    List<Venue> suggest(@Param("q") String q,
                        @Param("limit") int limit,
                        @Param("cityHint") String cityHint);


    @Query(value = """
      select json_build_object(
               'type', 'Feature',
               'geometry', ST_AsGeoJSON(COALESCE(v.area_geom, v.geom), 6)::json,
               'properties', json_build_object(
                   'id', v.id,
                   'name', v.name,
                   'city', c.slug
               )
             )::text
      from venues v
      left join cities c on c.id = v.city_id
      where v.id = :id
        and COALESCE(v.area_geom, v.geom) is not null
    """, nativeQuery = true)
    String getShapeAsGeoJson(@Param("id") UUID id);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
      UPDATE venues
      SET area_geom = ST_MakeValid(ST_SetSRID(ST_GeomFromGeoJSON(:geojson), 4326)),
          geom      = COALESCE(
                        geom,
                        ST_Centroid(
                          ST_MakeValid(ST_SetSRID(ST_GeomFromGeoJSON(:geojson), 4326))
                        )
                      )
      WHERE id = :id
      """, nativeQuery = true)
    int updateAreaGeomFromGeoJson(@Param("id") UUID id,
                                  @Param("geojson") String geojson);



}
