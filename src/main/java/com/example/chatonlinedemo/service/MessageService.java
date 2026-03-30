package com.example.chatonlinedemo.service;

import com.example.chatonlinedemo.dto.MessageDTO;
import com.example.chatonlinedemo.entity.Conversation;
import com.example.chatonlinedemo.entity.Message;
import com.example.chatonlinedemo.exception.BadRequestException;
import com.example.chatonlinedemo.exception.ResourceNotFoundException;
import com.example.chatonlinedemo.moderation.ContentModerationService;
import com.example.chatonlinedemo.repository.ConversationRepository;
import com.example.chatonlinedemo.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ContentModerationService moderationService;

    @Transactional
    public MessageDTO sendMessage(Long conversationId, Long senderId, String content, Message.MessageType type, String imageUrl) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        boolean isSensitive = moderationService.analyzeContent(content);

        Long receiverId = null;
        if (conversation.getUser1Id() != null && conversation.getUser1Id().equals(senderId)) {
            receiverId = conversation.getUser2Id();
        } else if (conversation.getUser2Id() != null && conversation.getUser2Id().equals(senderId)) {
            receiverId = conversation.getUser1Id();
        }

        Message message = Message.builder()
                .conversation(conversation)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .imageUrl(imageUrl)
                .type(type)
                .status(Message.MessageStatus.SENT)
                .isSensitive(isSensitive)
                .isFlagged(isSensitive)
                .build();

        message = messageRepository.save(message);

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return MessageDTO.fromEntity(message);
    }

    public Page<MessageDTO> getMessages(Long conversationId, int page, int size) {
        Page<Message> messages = messageRepository.findByConversationId(conversationId, PageRequest.of(page, size));
        return messages.map(MessageDTO::fromEntity);
    }

    public List<MessageDTO> getMessagesByConversationId(Long conversationId) {
        return messageRepository.findMessagesByConversationId(conversationId).stream()
                .map(MessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsDelivered(Long conversationId, Long userId) {
        messageRepository.updateMessageStatusForConversation(userId, conversationId, Message.MessageStatus.DELIVERED);
    }

    @Transactional
    public void markAsSeen(Long conversationId, Long userId) {
        messageRepository.updateMessageStatusForConversation(userId, conversationId, Message.MessageStatus.SEEN);
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    public Page<MessageDTO> searchMessages(String keyword, int page, int size) {
        return messageRepository.searchMessages(keyword, PageRequest.of(page, size))
                .map(MessageDTO::fromEntity);
    }

    public Page<MessageDTO> getFlaggedMessages(int page, int size) {
        return messageRepository.findFlaggedMessages(PageRequest.of(page, size))
                .map(MessageDTO::fromEntity);
    }
}
