package com.teamup.teamUp.service;

import com.teamup.teamUp.client.nominatim.NominatimClient;
import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.mapper.CityMapper;
import com.teamup.teamUp.model.dto.city.CityDto;
import com.teamup.teamUp.model.dto.city.CityUpsertRequestDto;
import com.teamup.teamUp.model.entity.City;
import com.teamup.teamUp.repository.CityRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CityService {
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;
    private final NominatimClient nominatimClient;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public CityService(CityRepository cityRepository, CityMapper cityMapper, NominatimClient nominatimClient) {
        this.cityRepository = cityRepository;
        this.cityMapper = cityMapper;
        this.nominatimClient = nominatimClient;
    }

    @Transactional(readOnly = true)
    public CityDto getBySlug(String slug){
        City city = cityRepository.findBySlug(slug.trim().toLowerCase()).orElseThrow(()->new NotFoundException("City not found"));
        return cityMapper.toDto(city);
    }

    @Transactional(readOnly = true)
    public List<CityDto> suggest(String q, int limit){
        if(q==null||q.isBlank()){
            return List.of();
        }
        return cityRepository.findAll().stream()
                .filter(c->c.getName()!=null && c.getName().toLowerCase().contains(q.toLowerCase()))
                .limit(Math.max(1,Math.min(limit,20)))
                .map(cityMapper::toDto)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CityDto upsert(CityUpsertRequestDto r) {
        var existing = cityRepository.findBySlug(r.slug().toLowerCase()).orElse(null);
        var c = existing != null ? existing : new City();
        c.setName(r.name().trim());
        c.setSlug(r.slug().trim().toLowerCase());
        c.setCenterLat(r.centerLat()); c.setCenterLng(r.centerLng());
        c.setMinLat(r.minLat()); c.setMinLng(r.minLng());
        c.setMaxLat(r.maxLat()); c.setMaxLng(r.maxLng());
        c.setCountryCode(r.countryCode());
        return cityMapper.toDto(cityRepository.save(c));
    }

    @Transactional
    public void importGeometryByCitySlug(String slug){
        var city = cityRepository.findBySlug(slug.trim().toLowerCase()).orElseThrow(()->new NotFoundException("City not found: "+slug));

        var q = city.getName();
        var geojsonOpt = nominatimClient.cityPolygonGeoJson(q+", Romania");
        var geojson = geojsonOpt.orElseThrow(()->new NotFoundException("Nominatim polygon not found for: "+q));

        int n = cityRepository.updateAreaGeomFromGeoJson(city.getId(), geojson);
        if(n==0){
            throw new IllegalStateException("Failed to update city geojson for: "+slug);
        }
    }


    @Transactional
    public int assignCitiesToVenuesByGeometry() {
        var sql = """
            UPDATE venues v
            SET city_id = c.id
            FROM cities c
            WHERE v.city_id IS NULL
              AND v.latitude  IS NOT NULL
              AND v.longitude IS NOT NULL
              AND c.area_geom IS NOT NULL
              AND ST_Contains(
                    c.area_geom,
                    ST_SetSRID(ST_MakePoint(v.longitude, v.latitude), 4326)
                  );
        """;
        return em.createNativeQuery(sql).executeUpdate();
    }


}
