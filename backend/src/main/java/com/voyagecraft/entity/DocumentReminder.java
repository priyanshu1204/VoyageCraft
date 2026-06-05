package com.voyagecraft.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_reminders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DocumentReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_document_id", nullable = false)
    private TravelDocument travelDocument;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    private LocalDate reminderDate;

    @Builder.Default
    private Boolean dismissed = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
