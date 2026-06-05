package com.voyagecraft.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quick_notes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class QuickNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "LONGTEXT")
    private String photoUrls; // comma-separated Base64 data-URIs or URLs

    @Column(length = 50)
    private String capturedLatitude;

    @Column(length = 50)
    private String capturedLongitude;

    @Column(length = 200)
    private String capturedLocationName;

    @Builder.Default
    private Boolean isSynced = true; // true = created online, false = created offline and synced later

    @Builder.Default
    private Boolean isPinned = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
