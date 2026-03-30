package com.example.chatonlinedemo.moderation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentModerationService {

    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
            "spam", "scam", "hack", "virus", "malware", "phishing", "fraud",
            "xxx", "porn", "nude", "nsfw", "adult"
    ));

    private static final Pattern URL_PATTERN = Pattern.compile("(https?://|www\\.)[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern REPEATED_CHARS_PATTERN = Pattern.compile("(.)\\1{4,}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", Pattern.CASE_INSENSITIVE);

    private static final int MAX_MESSAGES_PER_MINUTE = 5;
    private static final int MAX_MESSAGE_LENGTH = 5000;
    private static final int MIN_MESSAGE_LENGTH = 1;

    private final Map<Long, UserMessageTracker> userMessageTrackers = new ConcurrentHashMap<>();

    public boolean analyzeContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        content = content.trim();

        if (content.length() < MIN_MESSAGE_LENGTH || content.length() > MAX_MESSAGE_LENGTH) {
            return false;
        }

        if (containsSensitiveWords(content)) {
            log.warn("Sensitive words detected in message");
            return true;
        }

        if (containsExcessiveUrls(content)) {
            log.warn("Excessive URLs detected");
            return true;
        }

        if (containsExcessiveEmails(content)) {
            log.warn("Excessive emails detected");
            return true;
        }

        if (containsRepeatedPatterns(content)) {
            log.warn("Repeated patterns detected");
            return true;
        }

        return false;
    }

    public ModerationResult checkUserMessages(Long userId, String content) {
        UserMessageTracker tracker = userMessageTrackers.computeIfAbsent(userId, k -> new UserMessageTracker());
        tracker.cleanOldMessages();

        boolean isSensitive = analyzeContent(content);

        if (tracker.addMessage()) {
            return new ModerationResult(
                    isSensitive,
                    tracker.getMessageCount() > MAX_MESSAGES_PER_MINUTE,
                    tracker.getMessageCount(),
                    tracker.getMessageCount() > MAX_MESSAGES_PER_MINUTE ? "Too many messages per minute" : null
            );
        }

        return new ModerationResult(isSensitive, true, tracker.getMessageCount(), "Too many messages per minute");
    }

    public boolean isSpam(String content) {
        if (content == null) return false;

        String lowerContent = content.toLowerCase();
        
        int urlCount = countUrls(content);
        if (urlCount > 3) return true;

        if (EMAIL_PATTERN.matcher(content).find() && lowerContent.contains("contact")) {
            return true;
        }

        return false;
    }

    private boolean containsSensitiveWords(String content) {
        String lowerContent = content.toLowerCase();
        for (String word : SENSITIVE_WORDS) {
            if (lowerContent.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsExcessiveUrls(String content) {
        int urlCount = countUrls(content);
        return urlCount > 3;
    }

    private int countUrls(String content) {
        return (int) URL_PATTERN.matcher(content).results().count();
    }

    private boolean containsExcessiveEmails(String content) {
        int emailCount = (int) EMAIL_PATTERN.matcher(content).results().count();
        return emailCount > 2;
    }

    private boolean containsRepeatedPatterns(String content) {
        return REPEATED_CHARS_PATTERN.matcher(content).find();
    }

    public record ModerationResult(
            boolean isSensitive,
            boolean isSpam,
            int messageCount,
            String warningMessage
    ) {}

    private static class UserMessageTracker {
        private final java.util.List<Long> timestamps = new java.util.concurrent.CopyOnWriteArrayList<>();

        public boolean addMessage() {
            long now = System.currentTimeMillis();
            timestamps.add(now);
            return timestamps.size() <= MAX_MESSAGES_PER_MINUTE;
        }

        public int getMessageCount() {
            cleanOldMessages();
            return timestamps.size();
        }

        public void cleanOldMessages() {
            long oneMinuteAgo = System.currentTimeMillis() - 60000;
            timestamps.removeIf(ts -> ts < oneMinuteAgo);
        }
    }
}
