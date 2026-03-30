package com.example.chatonlinedemo.controller;

import com.example.chatonlinedemo.dto.*;
import com.example.chatonlinedemo.entity.Message;
import com.example.chatonlinedemo.security.CustomUserDetailsService;
import com.example.chatonlinedemo.service.ConversationService;
import com.example.chatonlinedemo.service.MessageService;
import com.example.chatonlinedemo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat APIs")
public class ChatController {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping("/conversations")
    @Operation(summary = "Get user conversations")
    public ResponseEntity<ApiResponse<List<ConversationDTO>>> getConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        List<ConversationDTO> conversations = conversationService.getUserConversations(user.getId());
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @PostMapping("/conversations")
    @Operation(summary = "Create or get private conversation")
    public ResponseEntity<ApiResponse<ConversationDTO>> createConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long otherUserId) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        ConversationDTO conversation = conversationService.createPrivateConversation(user.getId(), otherUserId);
        return ResponseEntity.ok(ApiResponse.success(conversation));
    }

    @GetMapping("/conversations/{id}")
    @Operation(summary = "Get conversation by ID")
    public ResponseEntity<ApiResponse<ConversationDTO>> getConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        ConversationDTO conversation = conversationService.getConversationById(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(conversation));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "Get messages in a conversation")
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<MessageDTO> messages = messageService.getMessages(conversationId, page, size);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "Send a message")
    public ResponseEntity<ApiResponse<MessageDTO>> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId,
            @RequestParam String content,
            @RequestParam(defaultValue = "TEXT") Message.MessageType type,
            @RequestParam(required = false) String imageUrl) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        MessageDTO message = messageService.sendMessage(conversationId, user.getId(), content, type, imageUrl);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/conversations/{conversationId}/messages/{messageId}/seen")
    @Operation(summary = "Mark messages as seen")
    public ResponseEntity<ApiResponse<Void>> markAsSeen(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId,
            @PathVariable Long messageId) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        messageService.markAsSeen(conversationId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Messages marked as seen", null));
    }

    @DeleteMapping("/conversations/{conversationId}/messages/{messageId}")
    @Operation(summary = "Delete a message")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success("Message deleted", null));
    }

    @GetMapping("/messages/search")
    @Operation(summary = "Search messages")
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> searchMessages(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<MessageDTO> messages = messageService.searchMessages(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }
}
