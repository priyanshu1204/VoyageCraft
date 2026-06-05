package com.voyagecraft.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "offline_caches", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"trip_id", "user_id"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OfflineCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String snapshotData; // Full JSON snapshot of trip data

    private Long versionNumber; // Incremented on each sync

    private LocalDateTime lastSyncedAt;

    @Builder.Default
    private Boolean reducedMode = false; // Whether to use compressed data

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
