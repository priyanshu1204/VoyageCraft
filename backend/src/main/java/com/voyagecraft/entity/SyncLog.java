package com.voyagecraft.entity;

import com.voyagecraft.enums.SyncStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sync_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false, length = 50)
    private String entityType; // e.g., ITINERARY_ITEM, TRANSPORT, STAY

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 20)
    private String action; // CREATE, UPDATE, DELETE

    @Column(columnDefinition = "TEXT")
    private String offlinePayload; // JSON of changes made offline

    @Column(columnDefinition = "TEXT")
    private String serverPayload; // JSON of current server state (for conflict)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SyncStatus syncStatus = SyncStatus.PENDING;

    @Column(length = 500)
    private String conflictMessage;

    private LocalDateTime offlineTimestamp; // when the change was made offline

    private LocalDateTime syncedAt; // when it was synced to server

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
