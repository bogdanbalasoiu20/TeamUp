package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.user.UserProfileResponseDto;
import com.teamup.teamUp.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserProfileResponseDto toProfileDto(User user, boolean isMyProfile){
        return new UserProfileResponseDto(
                user.getId(),
                user.getUsername(),
                isMyProfile ?user.getEmail():null,
                user.getBirthday(),
                isMyProfile ?user.getPhoneNumber():null,
                user.getPosition(),
                user.getCity(),
                user.getDescription(),
                user.getRank(),
                user.getPhotoUrl(),
                user.getCreatedAt());
    }
}
