package com.example.chatonlinedemo.controller;

import com.example.chatonlinedemo.dto.*;
import com.example.chatonlinedemo.entity.Message;
import com.example.chatonlinedemo.service.AdminService;
import com.example.chatonlinedemo.service.MessageService;
import com.example.chatonlinedemo.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin Management APIs")
public class AdminController {

    private final AdminService adminService;
    private final MessageService messageService;
    private final ReportService reportService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboard() {
        DashboardStats stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserDTO> users = adminService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/banned")
    @Operation(summary = "Get banned users")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getBannedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserDTO> users = adminService.getBannedUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user details")
    public ResponseEntity<ApiResponse<UserDTO>> getUser(@PathVariable Long id) {
        UserDTO user = adminService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/users/{id}/ban")
    @Operation(summary = "Ban a user")
    public ResponseEntity<ApiResponse<Void>> banUser(
            @PathVariable Long id,
            @RequestParam String reason) {
        adminService.banUser(id, 1L, reason);
        return ResponseEntity.ok(ApiResponse.success("User banned", null));
    }

    @PostMapping("/users/{id}/unban")
    @Operation(summary = "Unban a user")
    public ResponseEntity<ApiResponse<Void>> unbanUser(@PathVariable Long id) {
        adminService.unbanUser(id);
        return ResponseEntity.ok(ApiResponse.success("User unbanned", null));
    }

    @PostMapping("/users/{id}/reset-warnings")
    @Operation(summary = "Reset user warnings")
    public ResponseEntity<ApiResponse<Void>> resetWarnings(@PathVariable Long id) {
        adminService.resetWarnings(id);
        return ResponseEntity.ok(ApiResponse.success("Warnings reset", null));
    }

    @GetMapping("/messages/flagged")
    @Operation(summary = "Get flagged messages")
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> getFlaggedMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<MessageDTO> messages = messageService.getFlaggedMessages(page, size);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @DeleteMapping("/messages/{id}")
    @Operation(summary = "Delete a message")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.ok(ApiResponse.success("Message deleted", null));
    }

    @GetMapping("/reports")
    @Operation(summary = "Get pending reports")
    public ResponseEntity<ApiResponse<Page<ReportDTO>>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ReportDTO> reports = reportService.getPendingReports(page, size);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @PostMapping("/reports/{id}/resolve")
    @Operation(summary = "Resolve a report")
    public ResponseEntity<ApiResponse<ReportDTO>> resolveReport(
            @PathVariable Long id,
            @RequestParam String status) {
        var report = reportService.resolveReport(id, 1L, com.example.chatonlinedemo.entity.Report.ReportStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
