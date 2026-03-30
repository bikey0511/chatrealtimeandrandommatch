package com.example.chatonlinedemo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user1_id", nullable = false)
    private Long user1Id;

    @Column(name = "user2_id", nullable = false)
    private Long user2Id;

    @Column(name = "conversation_id")
    private Long conversationId;

    @Column(name = "is_revealed")
    @Builder.Default
    private boolean isRevealed = false;

    @Column(name = "user1_requested_reveal")
    @Builder.Default
    private boolean user1RequestedReveal = false;

    @Column(name = "user2_requested_reveal")
    @Builder.Default
    private boolean user2RequestedReveal = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "revealed_at")
    private LocalDateTime revealedAt;
}
