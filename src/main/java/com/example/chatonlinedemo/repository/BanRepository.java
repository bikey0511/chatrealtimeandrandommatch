package com.example.chatonlinedemo.repository;

import com.example.chatonlinedemo.entity.Ban;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BanRepository extends JpaRepository<Ban, Long> {
    
    @Query("SELECT b FROM Ban b WHERE b.userId = :userId AND b.isActive = true")
    Optional<Ban> findActiveBanByUserId(@Param("userId") Long userId);
    
    @Query("SELECT b FROM Ban b WHERE b.isActive = true AND b.banEnd <= :now")
    List<Ban> findExpiredBans(@Param("now") LocalDateTime now);
    
    @Query("SELECT b FROM Ban b WHERE b.userId = :userId ORDER BY b.banStart DESC")
    List<Ban> findBanHistoryByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Ban b SET b.isActive = false WHERE b.banEnd <= :now")
    void deactivateExpiredBans(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(b) FROM Ban b WHERE b.isActive = true")
    long countActiveBans();
}
