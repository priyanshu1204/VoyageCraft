package com.voyagecraft.entity;

import com.voyagecraft.enums.ChangeAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "change_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChangeAction action;

    @Column(nullable = false, length = 50)
    private String entityType; // "trip", "activity", "stay", etc.

    private Long entityId;

    @Column(nullable = false, length = 500)
    private String description;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
