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
public class MatchDTO {
    private Long id;
    private Long user1Id;
    private Long user2Id;
    private String user1Username;
    private String user2Username;
    private String user1Avatar;
    private String user2Avatar;
    private Long conversationId;
    private boolean isRevealed;
    private boolean canReveal;
    private LocalDateTime createdAt;
}
