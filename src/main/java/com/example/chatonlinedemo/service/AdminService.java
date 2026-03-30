package com.example.chatonlinedemo.service;

import com.example.chatonlinedemo.dto.DashboardStats;
import com.example.chatonlinedemo.dto.UserDTO;
import com.example.chatonlinedemo.entity.User;
import com.example.chatonlinedemo.exception.ResourceNotFoundException;
import com.example.chatonlinedemo.moderation.ModerationService;
import com.example.chatonlinedemo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ReportRepository reportRepository;
    private final BanRepository banRepository;
    private final ConversationRepository conversationRepository;
    private final ModerationService moderationService;

    public DashboardStats getDashboardStats() {
        return DashboardStats.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.findAllActiveUsers(PageRequest.of(0, 1)).getTotalElements())
                .onlineUsers(userRepository.countOnlineUsers())
                .totalMessages(messageRepository.count())
                .messagesToday(messageRepository.countMessagesSince(LocalDateTime.now().minusDays(1)))
                .totalMatches(conversationRepository.countRandomMatches())
                .pendingReports(reportRepository.countPendingReports())
                .activeBans(banRepository.countActiveBans())
                .totalReports(reportRepository.count())
                .build();
    }

    public Page<UserDTO> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
                .map(UserDTO::fromEntity);
    }

    public Page<UserDTO> getBannedUsers(int page, int size) {
        return userRepository.findAllBannedUsers(PageRequest.of(page, size))
                .map(UserDTO::fromEntity);
    }

    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserDTO.fromEntity(user);
    }

    @Transactional
    public void banUser(Long userId, Long adminId, String reason) {
        moderationService.permanentBan(userId, adminId, reason);
    }

    @Transactional
    public void unbanUser(Long userId) {
        moderationService.unbanUser(userId);
    }

    @Transactional
    public void resetWarnings(Long userId) {
        moderationService.resetWarningCount(userId);
    }

    public Page<UserDTO> searchUsers(String query, int page, int size) {
        Page<User> userPage = userRepository.findAll(PageRequest.of(page, size));
        List<UserDTO> filtered = userPage.getContent().stream()
                .filter(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()))
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        return new PageImpl<>(filtered, PageRequest.of(page, size), filtered.size());
    }
}
