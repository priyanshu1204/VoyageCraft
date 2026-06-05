package com.voyagecraft.entity;

import com.voyagecraft.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_documents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserDocument {
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
    @Column(nullable = false, length = 50)
    private DocumentType documentType;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 50)
    private String documentNumber;

    @Column(length = 100)
    private String issuingCountry;

    private LocalDate issueDate;
    private LocalDate expiryDate;

    @Column(length = 500)
    private String fileUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
