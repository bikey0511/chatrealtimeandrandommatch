package com.example.chatonlinedemo.repository;

import com.example.chatonlinedemo.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    @Query("SELECT m FROM Match m WHERE m.user1Id = :userId OR m.user2Id = :userId")
    List<Match> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Match m WHERE m.conversationId = :conversationId")
    Optional<Match> findByConversationId(@Param("conversationId") Long conversationId);
    
    @Query("SELECT m FROM Match m WHERE m.isRevealed = false AND (m.user1Id = :userId OR m.user2Id = :userId)")
    List<Match> findUnrevealedMatches(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Match m WHERE (m.user1Id = :user1Id AND m.user2Id = :user2Id) OR (m.user1Id = :user2Id AND m.user2Id = :user1Id)")
    Optional<Match> findExistingMatch(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
    
    @Query("SELECT COUNT(m) FROM Match m WHERE m.isRevealed = true")
    long countRevealedMatches();
}
