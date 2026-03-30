package com.example.chatonlinedemo.service;

import com.example.chatonlinedemo.dto.MatchDTO;
import com.example.chatonlinedemo.dto.MessageDTO;
import com.example.chatonlinedemo.entity.User;
import com.example.chatonlinedemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public void sendMessageToUser(Long userId, MessageDTO message) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/messages",
                message
        );
        log.debug("Message sent to user {}: {}", userId, message.getId());
    }

    public void sendTypingIndicator(Long receiverId, Long senderId, String conversationId, boolean isTyping) {
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/typing",
                new TypingPayload(senderId, conversationId, isTyping)
        );
    }

    public void sendMessageStatusUpdate(Long userId, Long messageId, String status) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/status",
                new StatusUpdatePayload(messageId, status)
        );
    }

    public void sendMatchFoundNotification(Long userId, MatchDTO match) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/match",
                match
        );
        log.info("Match notification sent to user {}", userId);
    }

    public void sendRevealRequest(Long userId, Long matchId) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/reveal-request",
                new RevealRequestPayload(matchId)
        );
    }

    public void sendIdentityRevealed(Long user1Id, Long user2Id, MatchDTO match) {
        messagingTemplate.convertAndSendToUser(
                user1Id.toString(),
                "/queue/revealed",
                match
        );
        messagingTemplate.convertAndSendToUser(
                user2Id.toString(),
                "/queue/revealed",
                match
        );
        log.info("Identity revealed for match {}", match.getId());
    }

    public void sendOnlineStatus(Long userId, boolean isOnline) {
        messagingTemplate.convertAndSend("/topic/online-status/" + userId, isOnline);
    }

    public void broadcastOnlineUsers() {
        userRepository.findAllOnlineUsers().forEach(user -> 
            messagingTemplate.convertAndSend("/topic/online-users", UserDTO.fromEntityBasic(user))
        );
    }

    public record TypingPayload(Long senderId, String conversationId, boolean isTyping) {}
    public record StatusUpdatePayload(Long messageId, String status) {}
    public record RevealRequestPayload(Long matchId) {}

    private static class UserDTO {
        private Long id;
        private String username;
        private String avatar;

        public static UserDTO fromEntityBasic(User user) {
            UserDTO dto = new UserDTO();
            dto.id = user.getId();
            dto.username = user.getUsername();
            dto.avatar = user.getAvatar();
            return dto;
        }
    }
}
