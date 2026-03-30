package com.example.chatonlinedemo.moderation;

import com.example.chatonlinedemo.entity.Ban;
import com.example.chatonlinedemo.entity.Notification;
import com.example.chatonlinedemo.entity.User;
import com.example.chatonlinedemo.repository.BanRepository;
import com.example.chatonlinedemo.repository.UserRepository;
import com.example.chatonlinedemo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationService {

    private final UserRepository userRepository;
    private final BanRepository banRepository;
    private final NotificationService notificationService;
    private final ContentModerationService contentModerationService;

    private static final int MAX_WARNINGS = 4;
    private static final int WARNING_THRESHOLD = 3;

    @Transactional
    public void handleViolation(Long userId, String reason) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        if (user.getStatus() == User.Status.BANNED || user.getStatus() == User.Status.TEMPORARY_BANNED) {
            return;
        }

        user.setWarningCount(user.getWarningCount() + 1);
        userRepository.save(user);

        log.info("User {} received warning. Total warnings: {}", userId, user.getWarningCount());

        if (user.getWarningCount() == WARNING_THRESHOLD) {
            notificationService.createNotification(
                    userId,
                    Notification.NotificationType.WARNING,
                    "Final Warning!",
                    "You have received 3 warnings. One more violation will result in a temporary ban.",
                    null
            );
        }

        if (user.getWarningCount() >= MAX_WARNINGS) {
            temporaryBan(user);
        }
    }

    @Transactional
    public void temporaryBan(User user) {
        Random random = new Random();
        int banHours = 2 + random.nextInt(5);

        Ban ban = Ban.builder()
                .userId(user.getId())
                .type(Ban.BanType.TEMPORARY)
                .reason("Exceeded maximum warnings (" + user.getWarningCount() + ")")
                .banStart(LocalDateTime.now())
                .banEnd(LocalDateTime.now().plusHours(banHours))
                .isActive(true)
                .build();

        banRepository.save(ban);

        user.setStatus(User.Status.TEMPORARY_BANNED);
        user.setWarningCount(0);
        userRepository.save(user);

        notificationService.createNotification(
                user.getId(),
                Notification.NotificationType.BAN,
                "You Have Been Temporarily Banned",
                "Due to repeated violations, you have been banned for " + banHours + " hours.",
                ban.getId()
        );

        log.info("User {} temporarily banned for {} hours", user.getId(), banHours);
    }

    @Transactional
    public void permanentBan(Long userId, Long adminId, String reason) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Ban ban = Ban.builder()
                .userId(userId)
                .bannedBy(adminId)
                .type(Ban.BanType.PERMANENT)
                .reason(reason)
                .banStart(LocalDateTime.now())
                .isActive(true)
                .build();

        banRepository.save(ban);

        user.setStatus(User.Status.BANNED);
        user.setWarningCount(0);
        userRepository.save(user);

        notificationService.createNotification(
                userId,
                Notification.NotificationType.BAN,
                "You Have Been Permanently Banned",
                "Your account has been permanently banned. Reason: " + reason,
                ban.getId()
        );

        log.info("User {} permanently banned by admin {}", userId, adminId);
    }

    @Transactional
    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        banRepository.findActiveBanByUserId(userId).ifPresent(ban -> {
            ban.setActive(false);
            banRepository.save(ban);
        });

        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);

        notificationService.createNotification(
                userId,
                Notification.NotificationType.UNBAN,
                "Account Unbanned",
                "Your account has been unbanned. Please follow community guidelines.",
                null
        );

        log.info("User {} unbanned", userId);
    }

    @Transactional
    public void resetWarningCount(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setWarningCount(0);
            userRepository.save(user);
            log.info("Warning count reset for user {}", userId);
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processExpiredBans() {
        LocalDateTime now = LocalDateTime.now();
        
        banRepository.findExpiredBans(now).forEach(ban -> {
            User user = userRepository.findById(ban.getUserId()).orElse(null);
            if (user != null && user.getStatus() == User.Status.TEMPORARY_BANNED) {
                user.setStatus(User.Status.ACTIVE);
                user.setWarningCount(0);
                userRepository.save(user);

                ban.setActive(false);
                banRepository.save(ban);

                notificationService.createNotification(
                        user.getId(),
                        Notification.NotificationType.UNBAN,
                        "Account Unbanned",
                        "Your temporary ban has expired. Welcome back!",
                        null
                );

                log.info("Auto-unbanned user {} after temporary ban expired", user.getId());
            }
        });
    }
}
