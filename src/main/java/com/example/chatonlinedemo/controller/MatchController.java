package com.example.chatonlinedemo.controller;

import com.example.chatonlinedemo.dto.*;
import com.example.chatonlinedemo.security.CustomUserDetailsService;
import com.example.chatonlinedemo.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
@Tag(name = "Match", description = "Random Match APIs")
public class MatchController {

    private final MatchService matchService;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/find")
    @Operation(summary = "Find random match")
    public ResponseEntity<ApiResponse<Map>> findMatch(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        Map result = matchService.findRandomMatch(user.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel waiting for match")
    public ResponseEntity<ApiResponse<Void>> cancelWaiting(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        matchService.cancelWaiting(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Cancelled", null));
    }

    @PostMapping("/{matchId}/reveal")
    @Operation(summary = "Request or reveal identity")
    public ResponseEntity<ApiResponse<MatchDTO>> revealIdentity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long matchId) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        MatchDTO match = matchService.revealIdentity(matchId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(match));
    }

    @GetMapping("/history")
    @Operation(summary = "Get match history")
    public ResponseEntity<ApiResponse<List<MatchDTO>>> getMatchHistory(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        List<MatchDTO> matches = matchService.getUserMatches(user.getId());
        return ResponseEntity.ok(ApiResponse.success(matches));
    }

    @GetMapping("/{matchId}")
    @Operation(summary = "Get match by ID")
    public ResponseEntity<ApiResponse<MatchDTO>> getMatch(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long matchId) {
        var user = userDetailsService.getUserByUsername(userDetails.getUsername());
        MatchDTO match = matchService.getMatchById(matchId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(match));
    }
}
