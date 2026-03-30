package com.example.chatonlinedemo.controller;

import com.example.chatonlinedemo.dto.*;
import com.example.chatonlinedemo.security.CustomUserDetailsService;
import com.example.chatonlinedemo.service.NotificationService;
import com.example.chatonlinedemo.service.ReportService;
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
@Tag(name = "Reports & Notifications", description = "Report and Notification APIs")
public class ReportController {

    private final ReportService reportService;
    private final NotificationService notificationService;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/reports")
    @Operation(summary = "Report a user")
    public ResponseEntity<ApiResponse<ReportDTO>> createReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ReportRequest request) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        ReportDTO report = reportService.createReport(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Report submitted", report));
    }

    @GetMapping("/notifications")
    @Operation(summary = "Get user notifications")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        Page<NotificationDTO> notifications = notificationService.getUserNotifications(user.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/notifications/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(user.getId());
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/notifications/unread/count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PostMapping("/notifications/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    @PostMapping("/notifications/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(ApiResponse.success("All marked as read", null));
    }
}
