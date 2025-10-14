package com.teamup.teamUp.service;

import com.teamup.teamUp.mapper.VenueMapper;
import com.teamup.teamUp.model.dto.venue.VenueResponseDto;
import com.teamup.teamUp.model.entity.Venue;
import com.teamup.teamUp.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueService {
    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;

    @Autowired
    public VenueService(VenueRepository venueRepository, VenueMapper venueMapper) {
        this.venueRepository = venueRepository;
        this.venueMapper = venueMapper;
    }

    public Page<VenueResponseDto> search(String city, String q, boolean activeOnly, Pageable pageable){
        String cityNorm = (city == null || city.isBlank()) ? null :city.trim();
        String qNorm = (q == null || q.isBlank()) ? null :q.trim();
        return venueRepository.search(cityNorm, qNorm, activeOnly,pageable).map(venueMapper::toDto);
    }

    public List<Venue> nearby(double lat, double lng, double radiusMeters, int limit){
        double dLat = radiusMeters / 111_320d; // ~ m per grad lat
        double metersPerDegLon = 111_320d * Math.cos(Math.toRadians(lat));
        double dLng = radiusMeters / Math.max(metersPerDegLon, 1e-6);

        double minLat = lat - dLat, maxLat = lat + dLat;
        double minLng = lng - dLng, maxLng = lng + dLng;

        var list = venueRepository.findInBBox(minLat, maxLat, minLng, maxLng);
        return list.stream()
                .limit(Math.max(1, Math.min(limit, 500)))
                .toList();
    }

}
