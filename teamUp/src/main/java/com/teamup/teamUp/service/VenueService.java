package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.mapper.VenueMapper;
import com.teamup.teamUp.model.dto.venue.VenueAdminUpdateRequestDto;
import com.teamup.teamUp.model.dto.venue.VenueResponseDto;
import com.teamup.teamUp.model.dto.venue.VenueUpsertRequestDto;
import com.teamup.teamUp.model.entity.Venue;
import com.teamup.teamUp.model.enums.VenueSource;
import com.teamup.teamUp.repository.CityRepository;
import com.teamup.teamUp.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class VenueService {
    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;
    private final CityRepository cityRepository;

    @Autowired
    public VenueService(VenueRepository venueRepository, VenueMapper venueMapper,  CityRepository cityRepository) {
        this.venueRepository = venueRepository;
        this.venueMapper = venueMapper;
        this.cityRepository = cityRepository;
    }

    @Transactional(readOnly = true)
    public VenueResponseDto findById(UUID id){
        var v = venueRepository.findById(id).orElseThrow(()->new NotFoundException("Venue not found"));
        return venueMapper.toDto(v);
    }

    public Page<VenueResponseDto> search(String city, String q, boolean activeOnly, Pageable pageable){
        String cityNorm = (city == null || city.isBlank()) ? null :city.trim();
        String qNorm = (q == null || q.isBlank()) ? null :q.trim();
        return venueRepository.search(cityNorm, qNorm, activeOnly,pageable).map(venueMapper::toDto);
    }

    public List<Venue> nearby(double lat, double lng, double radiusMeters, int limit){
//        double dLat = radiusMeters / 111_320d;
//        double metersPerDegLon = 111_320d * Math.cos(Math.toRadians(lat));
//        double dLng = radiusMeters / Math.max(metersPerDegLon, 1e-6);
//
//        double minLat = lat - dLat, maxLat = lat + dLat;
//        double minLng = lng - dLng, maxLng = lng + dLng;
//
//        var list = venueRepository.findInBBox(minLat, maxLat, minLng, maxLng);
//        return list.stream()
//                .limit(Math.max(1, Math.min(limit, 500)))
//                .toList();

        int lim = Math.max(1, Math.min(limit,500));
        return venueRepository.findNearbyOrdered(lat, lng, radiusMeters, lim);
    }

    @Transactional
    public Venue upsert(VenueUpsertRequestDto request) {
        boolean hasOsm = request.osmType() != null || request.osmId() != null;
        if ((request.osmType() == null) != (request.osmId() == null)) {
            throw new IllegalArgumentException("Both osmType and osmId must be provided together or both null.");
        }

        Venue v = hasOsm
                ? venueRepository.findByOsmTypeAndOsmId(request.osmType(), request.osmId())
                .orElseGet(() -> {
                    var nv = new Venue();
                    nv.setOsmType(request.osmType());
                    nv.setOsmId(request.osmId());
                    nv.setSource(VenueSource.OSM);
                    return nv;
                })
                : new Venue();

        boolean isNew = v.getId() == null;

        String name = trimOrNull(request.name());
        if (name == null || name.isBlank()) {
            Object tagsName = request.tagsJson() != null ? request.tagsJson().get("name") : null;
            name = tagsName instanceof String ? ((String) tagsName).trim() : null;
        }
        if (name == null || name.isBlank()) {
            name = "Unknown venue";
        }

        v.setName(name);
        v.setAddress(trimOrNull(request.address()));
        v.setPhoneNumber(trimOrNull(request.phoneNumber()));
        v.setLatitude(request.latitude());
        v.setLongitude(request.longitude());
        v.setTagsJson(request.tagsJson());

        if (request.city() != null && !request.city().isBlank()) {
            String raw = request.city().trim();
            String asSlug = slugify(raw);

            var city = cityRepository.findBySlug(asSlug)
                    .or(() -> cityRepository.findByNameIgnoreCase(raw))
                    .orElseThrow(() -> new NotFoundException("City not found: " + raw));

            v.setCity(city);
        } else {
            v.setCity(null);
        }

        if (v.getCity() == null && v.getLatitude() != null && v.getLongitude() != null) {
            cityRepository.findByPointGeom(v.getLatitude(), v.getLongitude())
                    .ifPresent(v::setCity);
        }

        if (isNew && !hasOsm) {
            v.setSource(VenueSource.ADMIN);
        }
        if (v.getIsActive() == null) v.setIsActive(true);

        return venueRepository.save(v);
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Transactional
    public Venue update(UUID id, VenueAdminUpdateRequestDto request) {
        Venue venue = venueRepository.findById(id).orElseThrow(()->new NotFoundException("Venue not found"));

        if(request.name()!=null){
            venue.setName(request.name().trim());
        }
        if(request.address()!=null){
            venue.setAddress(blankToNull(request.address()));
        }
        if(request.phoneNumber()!=null){
            venue.setPhoneNumber(blankToNull(request.phoneNumber()));
        }
        if (request.city() != null) {
            String val = request.city().trim();
            if (val.isEmpty()) {
                venue.setCity(null);
            } else {
                var city = cityRepository.findBySlug(val)
                        .orElseThrow(() -> new NotFoundException("City not found: " + val));
                venue.setCity(city);
            }
        }
        if(request.latitude()!=null){
            venue.setLatitude(request.latitude());
        }
        if(request.longitude()!=null){
            venue.setLongitude(request.longitude());
        }

        venue.setSource(VenueSource.ADMIN);
        return venue;
    }

    private String blankToNull(String s) {
        if(s==null) return null;
        var context = s.trim();
        return context.isEmpty()?null:context;
    }

    @Transactional
    public Venue setActive(UUID id, Boolean isActive){
        Venue venue = venueRepository.findById(id).orElseThrow(()->new NotFoundException("Venue not found: " + id));
        if(isActive!=null)
            venue.setIsActive(isActive);
        return venue;
    }

    public List<VenueResponseDto> inBBox(double minLat, double minLng, double maxLat, double maxLng, int limit){
        int lim = Math.max(1,Math.min(limit,500));
        return venueRepository.findInBBoxPostgis(minLat, minLng, maxLat, maxLng, lim).stream().map(venueMapper::toDto).toList();
    }

    public List<Venue> suggest(String q, int limit, String cityHint){
        String query = (q == null) ? "" : q.trim();
        if(query.isEmpty()) return List.of();

        int lim = Math.max(1,Math.min(limit,20));

        String hint = (cityHint==null||cityHint.isBlank()) ? null : cityHint.trim();

        return venueRepository.suggest(query, lim, hint);
    }

    public String getShape(UUID id){
        return venueRepository.getShapeAsGeoJson(id);
    }

    private static String slugify(String s) {
        String n = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        n = n.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return n;
    }


}
