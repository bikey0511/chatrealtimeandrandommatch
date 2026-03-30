package com.example.chatonlinedemo.repository;

import com.example.chatonlinedemo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE u.isOnline = true")
    List<User> findAllOnlineUsers();
    
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE'")
    Page<User> findAllActiveUsers(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.warningCount >= :count")
    List<User> findUsersWithWarnings(@Param("count") int count);
    
    @Query("SELECT u FROM User u WHERE u.role = 'USER' AND u.status = 'BANNED'")
    Page<User> findAllBannedUsers(Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    long countUsersCreatedSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isOnline = true")
    long countOnlineUsers();
}
