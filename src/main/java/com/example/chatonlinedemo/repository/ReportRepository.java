package com.example.chatonlinedemo.repository;

import com.example.chatonlinedemo.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    @Query("SELECT r FROM Report r WHERE r.status = 'PENDING' ORDER BY r.createdAt DESC")
    Page<Report> findPendingReports(Pageable pageable);
    
    @Query("SELECT r FROM Report r WHERE r.reportedUserId = :userId ORDER BY r.createdAt DESC")
    List<Report> findByReportedUserId(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Report r WHERE r.reporterId = :reporterId ORDER BY r.createdAt DESC")
    List<Report> findByReporterId(@Param("reporterId") Long reporterId);
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = 'PENDING'")
    long countPendingReports();
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.createdAt >= :since")
    long countReportsSince(@Param("since") LocalDateTime since);
}
