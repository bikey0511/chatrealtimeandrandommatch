package com.example.chatonlinedemo.service;

import com.example.chatonlinedemo.dto.ConversationDTO;
import com.example.chatonlinedemo.entity.Conversation;
import com.example.chatonlinedemo.entity.Message;
import com.example.chatonlinedemo.entity.User;
import com.example.chatonlinedemo.exception.BadRequestException;
import com.example.chatonlinedemo.exception.ResourceNotFoundException;
import com.example.chatonlinedemo.repository.ConversationRepository;
import com.example.chatonlinedemo.repository.MessageRepository;
import com.example.chatonlinedemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ConversationDTO createPrivateConversation(Long user1Id, Long user2Id) {
        Optional<Conversation> existing = conversationRepository.findPrivateConversation(user1Id, user2Id);
        if (existing.isPresent()) {
            return toDTO(existing.get(), user1Id);
        }

        Conversation conversation = Conversation.builder()
                .type(Conversation.ConversationType.PRIVATE)
                .user1Id(user1Id)
                .user2Id(user2Id)
                .build();

        conversation = conversationRepository.save(conversation);
        return toDTO(conversation, user1Id);
    }

    @Transactional
    public ConversationDTO createRandomConversation(Long user1Id, Long user2Id) {
        Conversation conversation = Conversation.builder()
                .type(Conversation.ConversationType.RANDOM)
                .user1Id(user1Id)
                .user2Id(user2Id)
                .build();

        conversation = conversationRepository.save(conversation);
        return toDTO(conversation, user1Id);
    }

    public List<ConversationDTO> getUserConversations(Long userId) {
        return conversationRepository.findByUserId(userId).stream()
                .map(conv -> toDTO(conv, userId))
                .collect(Collectors.toList());
    }

    public ConversationDTO getConversationById(Long id, Long userId) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        return toDTO(conversation, userId);
    }

    public Optional<Conversation> findPrivateConversation(Long user1Id, Long user2Id) {
        return conversationRepository.findPrivateConversation(user1Id, user2Id);
    }

    private ConversationDTO toDTO(Conversation conversation, Long currentUserId) {
        User user1 = userRepository.findById(conversation.getUser1Id()).orElse(null);
        User user2 = userRepository.findById(conversation.getUser2Id()).orElse(null);

        String lastMessage = "";
        LocalDateTime lastMessageTime = null;
        
        List<Message> messages = messageRepository.findMessagesByConversationId(conversation.getId());
        if (!messages.isEmpty()) {
            Message lastMsg = messages.get(0);
            lastMessage = lastMsg.getContent();
            lastMessageTime = lastMsg.getTimestamp();
        }

        return ConversationDTO.builder()
                .id(conversation.getId())
                .type(conversation.getType().name())
                .user1Id(conversation.getUser1Id())
                .user2Id(conversation.getUser2Id())
                .user1Username(user1 != null ? user1.getUsername() : "Unknown")
                .user2Username(user2 != null ? user2.getUsername() : "Unknown")
                .user1Avatar(user1 != null ? user1.getAvatar() : null)
                .user2Avatar(user2 != null ? user2.getAvatar() : null)
                .lastMessage(lastMessage)
                .lastMessageTime(lastMessageTime)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}
