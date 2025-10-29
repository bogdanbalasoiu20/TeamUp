package com.teamup.teamUp.service;

import com.teamup.teamUp.exceptions.NotFoundException;
import com.teamup.teamUp.mapper.CityMapper;
import com.teamup.teamUp.model.dto.city.CityDto;
import com.teamup.teamUp.model.dto.city.CityUpsertRequestDto;
import com.teamup.teamUp.model.entity.City;
import com.teamup.teamUp.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CityService {
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    @Autowired
    public CityService(CityRepository cityRepository,  CityMapper cityMapper) {
        this.cityRepository = cityRepository;
        this.cityMapper = cityMapper;
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


}
