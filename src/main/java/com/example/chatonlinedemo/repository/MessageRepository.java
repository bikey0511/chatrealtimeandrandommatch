package com.example.chatonlinedemo.repository;

import com.example.chatonlinedemo.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.timestamp DESC")
    Page<Message> findByConversationId(@Param("conversationId") Long conversationId, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.timestamp DESC")
    List<Message> findMessagesByConversationId(@Param("conversationId") Long conversationId);
    
    @Query("SELECT m FROM Message m WHERE m.receiverId = :userId AND m.status = 'SENT' OR m.status = 'DELIVERED'")
    List<Message> findUndeliveredMessages(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Message m WHERE m.isFlagged = true ORDER BY m.timestamp DESC")
    Page<Message> findFlaggedMessages(Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.senderId = :userId ORDER BY m.timestamp DESC")
    List<Message> findBySenderId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.timestamp >= :since")
    long countMessagesSince(@Param("since") LocalDateTime since);
    
    @Modifying
    @Query("UPDATE Message m SET m.status = :status WHERE m.receiverId = :userId AND m.conversation.id = :conversationId")
    void updateMessageStatusForConversation(@Param("userId") Long userId, @Param("conversationId") Long conversationId, @Param("status") Message.MessageStatus status);
    
    @Query("SELECT m FROM Message m WHERE m.content LIKE %:keyword% ORDER BY m.timestamp DESC")
    Page<Message> searchMessages(@Param("keyword") String keyword, Pageable pageable);
}
