package com.example.chatonlinedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private Long id;
    private String type;
    private Long user1Id;
    private Long user2Id;
    private String user1Username;
    private String user2Username;
    private String user1Avatar;
    private String user2Avatar;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
    private boolean isRevealed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
