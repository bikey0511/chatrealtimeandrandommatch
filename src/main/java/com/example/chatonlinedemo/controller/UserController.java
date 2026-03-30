package com.example.chatonlinedemo.controller;

import com.example.chatonlinedemo.dto.*;
import com.example.chatonlinedemo.security.CustomUserDetailsService;
import com.example.chatonlinedemo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User Management APIs")
public class UserController {

    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDTO user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest request) {
        UserDTO user = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/online")
    @Operation(summary = "Get all online users")
    public ResponseEntity<ApiResponse<Object>> getOnlineUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getOnlineUsers()));
    }

    @PostMapping("/{id}/block")
    @Operation(summary = "Block a user")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        userService.blockUser(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("User blocked", null));
    }

    @PostMapping("/{id}/unblock")
    @Operation(summary = "Unblock a user")
    public ResponseEntity<ApiResponse<Void>> unblockUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        userService.unblockUser(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("User unblocked", null));
    }
}
