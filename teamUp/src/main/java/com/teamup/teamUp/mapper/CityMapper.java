package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.city.CityDto;
import com.teamup.teamUp.model.entity.City;
import org.springframework.stereotype.Component;

@Component
public class CityMapper {
    public CityDto toDto(City c) {
        return new CityDto(
                c.getId(), c.getName(), c.getSlug(),
                c.getCenterLat(), c.getCenterLng(),
                c.getMinLat(), c.getMinLng(), c.getMaxLat(), c.getMaxLng(),
                c.getCountryCode()
        );
    }
}
