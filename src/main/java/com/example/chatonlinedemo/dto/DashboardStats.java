package com.example.chatonlinedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalUsers;
    private long activeUsers;
    private long onlineUsers;
    private long totalMessages;
    private long messagesToday;
    private long totalMatches;
    private long pendingReports;
    private long activeBans;
    private long totalReports;
}
