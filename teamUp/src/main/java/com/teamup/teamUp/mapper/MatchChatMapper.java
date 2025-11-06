package com.teamup.teamUp.mapper;

import com.teamup.teamUp.model.dto.chat.MessageResponseDto;
import com.teamup.teamUp.model.entity.MatchChatMessage;

public class MatchChatMapper {
    public static MessageResponseDto toDto(MatchChatMessage message){
        return new MessageResponseDto(
                message.getId(),
                message.getMatch().getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
