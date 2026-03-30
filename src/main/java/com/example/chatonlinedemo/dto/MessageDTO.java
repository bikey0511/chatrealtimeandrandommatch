package com.example.chatonlinedemo.dto;

import com.example.chatonlinedemo.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private Long receiverId;
    private String receiverUsername;
    private String content;
    private String imageUrl;
    private LocalDateTime timestamp;
    private String status;
    private String type;
    private boolean isSensitive;
    private boolean isFlagged;
    
    public static MessageDTO fromEntity(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .imageUrl(message.getImageUrl())
                .timestamp(message.getTimestamp())
                .status(message.getStatus().name())
                .type(message.getType().name())
                .isSensitive(message.isSensitive())
                .isFlagged(message.isFlagged())
                .build();
    }
}
