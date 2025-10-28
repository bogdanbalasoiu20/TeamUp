package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.venue.VenueResponseDto;
import com.teamup.teamUp.model.entity.Venue;
import org.springframework.stereotype.Component;

@Component
public class VenueMapper {
    public VenueResponseDto toDto(Venue v){
        return new VenueResponseDto(
                v.getId(), v.getName(), v.getAddress(), v.getPhoneNumber(), v.getCity().getSlug(),
                v.getLatitude(), v.getLongitude(),
                v.getOsmType(), v.getOsmId(),
                v.getTagsJson(),
                v.getSource().name(), v.getIsActive(),
                v.getCreatedAt(), v.getUpdatedAt()
        );
    }
}
