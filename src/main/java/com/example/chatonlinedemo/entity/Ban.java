package com.example.chatonlinedemo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ban {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "banned_by")
    private Long bannedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BanType type = BanType.TEMPORARY;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "ban_start")
    @Builder.Default
    private LocalDateTime banStart = LocalDateTime.now();

    @Column(name = "ban_end")
    private LocalDateTime banEnd;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isActive() {
        return isActive;
    }

    public enum BanType {
        TEMPORARY, PERMANENT
    }
}
