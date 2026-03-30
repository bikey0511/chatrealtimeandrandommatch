package com.example.chatonlinedemo.repository;

import com.example.chatonlinedemo.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    @Query("SELECT c FROM Conversation c WHERE c.user1Id = :userId OR c.user2Id = :userId ORDER BY c.updatedAt DESC")
    List<Conversation> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Conversation c WHERE (c.user1Id = :user1Id AND c.user2Id = :user2Id) OR (c.user1Id = :user2Id AND c.user2Id = :user1Id)")
    Optional<Conversation> findPrivateConversation(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
    
    @Query("SELECT c FROM Conversation c WHERE c.type = 'RANDOM' AND c.user1Id = :userId OR c.user2Id = :userId")
    Optional<Conversation> findRandomConversation(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Conversation c WHERE c.type = 'GROUP'")
    Page<Conversation> findAllGroupConversations(Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.type = 'RANDOM'")
    long countRandomMatches();
}
