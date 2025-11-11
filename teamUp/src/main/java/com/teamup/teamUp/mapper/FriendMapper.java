package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.friend.FriendRequestResponseDto;
import com.teamup.teamUp.model.dto.friend.FriendshipResponseDto;
import com.teamup.teamUp.model.entity.FriendRequest;
import com.teamup.teamUp.model.entity.Friendship;
import org.springframework.stereotype.Component;

@Component
public class FriendMapper {
    public static FriendRequestResponseDto toFriendRequestResponseDto(FriendRequest fr) {
        return new FriendRequestResponseDto(
                fr.getId(),
                fr.getRequester().getId(),
                fr.getRequester().getUsername(),
                fr.getAddressee().getId(),
                fr.getAddressee().getUsername(),
                fr.getStatus(),
                fr.getMessage(),
                fr.getCreatedAt(),
                fr.getRespondedAt()
        );
    }

    public static FriendshipResponseDto toFriendshipResponseDto(Friendship f,String currentUsername) {
        var friend = f.getUserA().getUsername().equals(currentUsername) ? f.getUserB() :  f.getUserA();

        return new FriendshipResponseDto(
                friend.getId(),
                friend.getUsername(),
                friend.getCity(),
                f.getCreatedAt()
        );
    }
}
