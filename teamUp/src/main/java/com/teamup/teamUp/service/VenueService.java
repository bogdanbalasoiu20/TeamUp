package com.teamup.teamUp.service;

import com.teamup.teamUp.mapper.VenueMapper;
import com.teamup.teamUp.model.dto.venue.VenueResponseDto;
import com.teamup.teamUp.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

}
