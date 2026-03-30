package com.example.chatonlinedemo.service;

import com.example.chatonlinedemo.dto.ReportDTO;
import com.example.chatonlinedemo.dto.ReportRequest;
import com.example.chatonlinedemo.entity.Notification;
import com.example.chatonlinedemo.entity.Report;
import com.example.chatonlinedemo.entity.User;
import com.example.chatonlinedemo.exception.BadRequestException;
import com.example.chatonlinedemo.exception.ResourceNotFoundException;
import com.example.chatonlinedemo.repository.ReportRepository;
import com.example.chatonlinedemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ReportDTO createReport(Long reporterId, ReportRequest request) {
        if (reporterId.equals(request.getReportedUserId())) {
            throw new BadRequestException("You cannot report yourself");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporter not found"));
        User reportedUser = userRepository.findById(request.getReportedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Reported user not found"));

        Report report = Report.builder()
                .reporterId(reporterId)
                .reportedUserId(request.getReportedUserId())
                .messageId(request.getMessageId())
                .reason(request.getReason())
                .description(request.getDescription())
                .status(Report.ReportStatus.PENDING)
                .build();

        report = reportRepository.save(report);
        return toDTO(report, reporter.getUsername(), reportedUser.getUsername());
    }

    public Page<ReportDTO> getPendingReports(int page, int size) {
        return reportRepository.findPendingReports(PageRequest.of(page, size))
                .map(report -> {
                    String reporterName = userRepository.findById(report.getReporterId())
                            .map(User::getUsername).orElse("Unknown");
                    String reportedName = userRepository.findById(report.getReportedUserId())
                            .map(User::getUsername).orElse("Unknown");
                    return toDTO(report, reporterName, reportedName);
                });
    }

    @Transactional
    public ReportDTO resolveReport(Long reportId, Long adminId, Report.ReportStatus status) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        report.setStatus(status);
        report.setResolvedBy(admin);
        report.setResolvedAt(java.time.LocalDateTime.now());

        report = reportRepository.save(report);

        String reporterName = userRepository.findById(report.getReporterId())
                .map(User::getUsername).orElse("Unknown");
        String reportedName = userRepository.findById(report.getReportedUserId())
                .map(User::getUsername).orElse("Unknown");

        return toDTO(report, reporterName, reportedName);
    }

    private ReportDTO toDTO(Report report, String reporterName, String reportedName) {
        return ReportDTO.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .reporterUsername(reporterName)
                .reportedUserId(report.getReportedUserId())
                .reportedUsername(reportedName)
                .messageId(report.getMessageId())
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus().name())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
