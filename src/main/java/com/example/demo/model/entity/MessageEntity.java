package com.example.demo.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {

    private Long id;

    private String nickname;

    private String contact;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
