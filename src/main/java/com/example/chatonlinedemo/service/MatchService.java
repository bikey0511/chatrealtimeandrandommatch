package com.example.chatonlinedemo.service;

import com.example.chatonlinedemo.dto.ConversationDTO;
import com.example.chatonlinedemo.dto.MatchDTO;
import com.example.chatonlinedemo.entity.Match;
import com.example.chatonlinedemo.entity.Notification;
import com.example.chatonlinedemo.entity.User;
import com.example.chatonlinedemo.exception.BadRequestException;
import com.example.chatonlinedemo.exception.ResourceNotFoundException;
import com.example.chatonlinedemo.repository.MatchRepository;
import com.example.chatonlinedemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final NotificationService notificationService;
    private final WebSocketService webSocketService;

    private final Set<Long> waitingUsers = ConcurrentHashMap.newKeySet();

    @Transactional
    public Map<String, Object> findRandomMatch(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != User.Status.ACTIVE) {
            throw new BadRequestException("Cannot find match: User account is not active");
        }

        Optional<Match> existingMatch = matchRepository.findByUserId(userId).stream()
                .filter(m -> !m.isRevealed() && m.getConversationId() != null)
                .findFirst();

        if (existingMatch.isPresent()) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "already_matched");
            result.put("match", toDTO(existingMatch.get(), userId));
            return result;
        }

        waitingUsers.add(userId);

        for (Long waitingUserId : waitingUsers) {
            if (!waitingUserId.equals(userId)) {
                waitingUsers.remove(waitingUserId);
                waitingUsers.remove(userId);

                Match match = Match.builder()
                        .user1Id(userId)
                        .user2Id(waitingUserId)
                        .build();
                match = matchRepository.save(match);

                ConversationDTO conversation = conversationService.createRandomConversation(userId, waitingUserId);
                match.setConversationId(conversation.getId());
                matchRepository.save(match);

                webSocketService.sendMatchFoundNotification(userId, toDTO(match, userId));
                webSocketService.sendMatchFoundNotification(waitingUserId, toDTO(match, waitingUserId));

                Map<String, Object> result = new HashMap<>();
                result.put("status", "matched");
                result.put("match", toDTO(match, userId));
                result.put("conversationId", conversation.getId());
                return result;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", "waiting");
        result.put("position", waitingUsers.size());
        return result;
    }

    @Transactional
    public void cancelWaiting(Long userId) {
        waitingUsers.remove(userId);
    }

    @Transactional
    public MatchDTO requestReveal(Long matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        if (!match.getUser1Id().equals(userId) && !match.getUser2Id().equals(userId)) {
            throw new BadRequestException("You are not part of this match");
        }

        if (match.isRevealed()) {
            throw new BadRequestException("Identity already revealed");
        }

        if (match.getUser1Id().equals(userId)) {
            match.setUser1RequestedReveal(true);
        } else {
            match.setUser2RequestedReveal(true);
        }

        match = matchRepository.save(match);

        Long otherUserId = match.getUser1Id().equals(userId) ? match.getUser2Id() : match.getUser1Id();
        notificationService.createNotification(otherUserId, Notification.NotificationType.REVEAL_REQUEST,
                "Reveal Identity Request", "Your match wants to reveal identities", matchId);

        webSocketService.sendRevealRequest(otherUserId, matchId);

        return toDTO(match, userId);
    }

    @Transactional
    public MatchDTO revealIdentity(Long matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        if (!match.getUser1Id().equals(userId) && !match.getUser2Id().equals(userId)) {
            throw new BadRequestException("You are not part of this match");
        }

        boolean userRequested = match.getUser1Id().equals(userId) ? match.isUser1RequestedReveal() : match.isUser2RequestedReveal();
        boolean otherRequested = match.getUser1Id().equals(userId) ? match.isUser2RequestedReveal() : match.isUser1RequestedReveal();

        if (!userRequested || !otherRequested) {
            return requestReveal(matchId, userId);
        }

        match.setRevealed(true);
        match.setRevealedAt(LocalDateTime.now());
        match = matchRepository.save(match);

        Long otherUserId = match.getUser1Id().equals(userId) ? match.getUser2Id() : match.getUser1Id();
        notificationService.createNotification(otherUserId, Notification.NotificationType.REVEAL_ACCEPTED,
                "Identity Revealed!", "Both users have agreed to reveal identities", matchId);

        webSocketService.sendIdentityRevealed(userId, otherUserId, toDTO(match, userId));

        return toDTO(match, userId);
    }

    public List<MatchDTO> getUserMatches(Long userId) {
        return matchRepository.findByUserId(userId).stream()
                .map(match -> toDTO(match, userId))
                .collect(Collectors.toList());
    }

    public MatchDTO getMatchById(Long matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        return toDTO(match, userId);
    }

    private MatchDTO toDTO(Match match, Long currentUserId) {
        User user1 = userRepository.findById(match.getUser1Id()).orElse(null);
        User user2 = userRepository.findById(match.getUser2Id()).orElse(null);

        boolean isUser1 = match.getUser1Id().equals(currentUserId);
        boolean canReveal = isUser1 ? !match.isUser1RequestedReveal() : !match.isUser2RequestedReveal();

        return MatchDTO.builder()
                .id(match.getId())
                .user1Id(match.getUser1Id())
                .user2Id(match.getUser2Id())
                .user1Username(match.isRevealed() || canReveal ? (user1 != null ? user1.getUsername() : "Unknown") : "Hidden")
                .user2Username(match.isRevealed() || canReveal ? (user2 != null ? user2.getUsername() : "Unknown") : "Hidden")
                .user1Avatar(match.isRevealed() || canReveal ? (user1 != null ? user1.getAvatar() : null) : null)
                .user2Avatar(match.isRevealed() || canReveal ? (user2 != null ? user2.getAvatar() : null) : null)
                .conversationId(match.getConversationId())
                .isRevealed(match.isRevealed())
                .canReveal(canReveal)
                .createdAt(match.getCreatedAt())
                .build();
    }
}
