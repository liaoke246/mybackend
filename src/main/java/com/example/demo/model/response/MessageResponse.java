package com.example.demo.model.response;

import com.example.demo.model.entity.MessageEntity;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class MessageResponse {

    Long id;

    String nickname;

    String contact;

    String content;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    public static MessageResponse fromEntity(MessageEntity entity) {
        return MessageResponse.builder()
                .id(entity.getId())
                .nickname(entity.getNickname())
                .contact(entity.getContact())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
