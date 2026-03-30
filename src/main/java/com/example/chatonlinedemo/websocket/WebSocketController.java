package com.example.chatonlinedemo.websocket;

import com.example.chatonlinedemo.dto.MessageDTO;
import com.example.chatonlinedemo.entity.Message;
import com.example.chatonlinedemo.entity.User;
import com.example.chatonlinedemo.moderation.ContentModerationService;
import com.example.chatonlinedemo.moderation.ModerationService;
import com.example.chatonlinedemo.security.CustomUserDetailsService;
import com.example.chatonlinedemo.service.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final MessageService messageService;
    private final WebSocketService webSocketService;
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final ContentModerationService moderationService;
    private final ModerationService moderationServiceAdmin;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = headerAccessor.getUser().getName();
            User user = userDetailsService.getUserByUsername(username);

            ContentModerationService.ModerationResult result = moderationService.checkUserMessages(user.getId(), request.getContent());

            if (result.isSpam()) {
                log.warn("User {} is sending too many messages", user.getId());
                return;
            }

            MessageDTO message = messageService.sendMessage(
                    request.getConversationId(),
                    user.getId(),
                    request.getContent(),
                    Message.MessageType.valueOf(request.getType() != null ? request.getType() : "TEXT"),
                    request.getImageUrl()
            );

            if (result.isSensitive() && message.isFlagged()) {
                moderationServiceAdmin.handleViolation(user.getId(), "Sensitive content detected");
            }

            if (message.getReceiverId() != null) {
                webSocketService.sendMessageToUser(message.getReceiverId(), message);
            }

            webSocketService.sendMessageToUser(user.getId(), message);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingRequest request, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = headerAccessor.getUser().getName();
            User user = userDetailsService.getUserByUsername(username);

            if (request.getReceiverId() != null) {
                webSocketService.sendTypingIndicator(request.getReceiverId(), user.getId(), request.getConversationId(), request.isTyping());
            }
        } catch (Exception e) {
            log.error("Error handling typing", e);
        }
    }

    @MessageMapping("/chat.seen")
    public void handleSeen(@Payload SeenRequest request, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = headerAccessor.getUser().getName();
            User user = userDetailsService.getUserByUsername(username);

            messageService.markAsSeen(request.getConversationId(), user.getId());

            if (request.getReceiverId() != null) {
                webSocketService.sendMessageStatusUpdate(request.getReceiverId(), request.getMessageId(), "SEEN");
            }
        } catch (Exception e) {
            log.error("Error handling seen", e);
        }
    }

    @MessageMapping("/chat.online")
    public void handleOnline(SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = headerAccessor.getUser().getName();
            userService.setUserOnline(username);
            webSocketService.sendOnlineStatus(userDetailsService.getUserByUsername(username).getId(), true);
        } catch (Exception e) {
            log.error("Error handling online status", e);
        }
    }

    @MessageMapping("/chat.offline")
    public void handleOffline(SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = headerAccessor.getUser().getName();
            userService.setUserOffline(username);
            User user = userDetailsService.getUserByUsername(username);
            webSocketService.sendOnlineStatus(user.getId(), false);
        } catch (Exception e) {
            log.error("Error handling offline status", e);
        }
    }

    @Data
    public static class MessageRequest {
        private Long conversationId;
        private String content;
        private String type;
        private String imageUrl;
    }

    @Data
    public static class TypingRequest {
        private Long receiverId;
        private String conversationId;
        private boolean typing;
    }

    @Data
    public static class SeenRequest {
        private Long receiverId;
        private Long conversationId;
        private Long messageId;
    }
}
