package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.mapper.VenueMapper;
import com.teamup.teamUp.model.dto.venue.VenueAdminUpdateRequestDto;
import com.teamup.teamUp.model.dto.venue.VenueResponseDto;
import com.teamup.teamUp.model.dto.venue.VenueUpsertRequestDto;
import com.teamup.teamUp.model.entity.Venue;
import com.teamup.teamUp.model.enums.VenueSource;
import com.teamup.teamUp.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VenueService {
    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;

    @Autowired
    public VenueService(VenueRepository venueRepository, VenueMapper venueMapper) {
        this.venueRepository = venueRepository;
        this.venueMapper = venueMapper;
    }

    public Venue findById(UUID id){
        return venueRepository.findById(id).orElseThrow(()->new NotFoundException("Venue not found"));
    }

    public Page<VenueResponseDto> search(String city, String q, boolean activeOnly, Pageable pageable){
        String cityNorm = (city == null || city.isBlank()) ? null :city.trim();
        String qNorm = (q == null || q.isBlank()) ? null :q.trim();
        return venueRepository.search(cityNorm, qNorm, activeOnly,pageable).map(venueMapper::toDto);
    }

    public List<Venue> nearby(double lat, double lng, double radiusMeters, int limit){
        double dLat = radiusMeters / 111_320d;
        double metersPerDegLon = 111_320d * Math.cos(Math.toRadians(lat));
        double dLng = radiusMeters / Math.max(metersPerDegLon, 1e-6);

        double minLat = lat - dLat, maxLat = lat + dLat;
        double minLng = lng - dLng, maxLng = lng + dLng;

        var list = venueRepository.findInBBox(minLat, maxLat, minLng, maxLng);
        return list.stream()
                .limit(Math.max(1, Math.min(limit, 500)))
                .toList();
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
        v.setCity(trimOrNull(request.city()));
        v.setLatitude(request.latitude());
        v.setLongitude(request.longitude());
        v.setTagsJson(request.tagsJson());

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
        if(request.city()!=null){
            venue.setCity(blankToNull(request.city()));
        }

        venue.setSource(VenueSource.ADMIN);
        return venue;
    }

    private String blankToNull(String s) {
        if(s==null) return null;
        var context = s.trim();
        return context.isEmpty()?null:context;
    }

}
