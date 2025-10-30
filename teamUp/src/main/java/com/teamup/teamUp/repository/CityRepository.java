package com.teamup.teamUp.repository;

import com.teamup.teamUp.model.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {
    Optional<City> findBySlug(String slug);
    boolean existsBySlug(String slug);
    Optional<City> findByNameIgnoreCase(String name);

    //import/actualizare orase din GeoJSON
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
      UPDATE cities
      SET area_geom =
        ST_Multi(           -- forțează Multi*
          ST_CollectionExtract(
            ST_MakeValid(ST_SetSRID(ST_GeomFromGeoJSON(:geojson), 4326)),
            3               -- 3 = POLYGON/MULTIPOLYGON
          )
        )
      WHERE id = :id
      """, nativeQuery = true)
    int updateAreaGeomFromGeoJson(@Param("id") UUID id, @Param("geojson") String geojson);

    //cauta orasul care contine punctul
    @Query(value = """
      SELECT c.* FROM cities c
      WHERE c.area_geom IS NOT NULL
        AND ST_Contains(c.area_geom, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326))
      ORDER BY ST_Distance(
               ST_Centroid(c.area_geom),
               ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
             ) ASC
      LIMIT 1
      """, nativeQuery = true)
    Optional<City> findByPointGeom(@Param("lat") double lat, @Param("lng") double lng);


}
