package com.example.chatonlinedemo.dto;

import com.example.chatonlinedemo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String avatar;
    private String bio;
    private String facebookLink;
    private String role;
    private String status;
    private int warningCount;
    private boolean isOnline;
    private LocalDateTime lastSeen;
    private LocalDateTime createdAt;
    
    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .facebookLink(user.getFacebookLink())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .warningCount(user.getWarningCount())
                .isOnline(user.isOnline())
                .lastSeen(user.getLastSeen())
                .createdAt(user.getCreatedAt())
                .build();
    }
    
    public static UserDTO fromEntityBasic(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .build();
    }
}
