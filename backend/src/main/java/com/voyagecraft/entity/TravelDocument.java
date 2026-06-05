package com.voyagecraft.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "travel_documents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TravelDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String countryName;

    @Column(length = 10)
    private String countryCode;

    @Column(columnDefinition = "TEXT")
    private String visaRequirements;

    @Column(columnDefinition = "TEXT")
    private String entryGuidelines;

    @Column(length = 500)
    private String officialLink;

    @Column(columnDefinition = "TEXT")
    private String additionalNotes;

    @OneToMany(mappedBy = "travelDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentChecklist> checklistItems = new ArrayList<>();

    @OneToMany(mappedBy = "travelDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentReminder> reminders = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
