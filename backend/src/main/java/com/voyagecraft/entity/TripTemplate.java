package com.voyagecraft.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_templates")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TripTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String destinationsJson;

    private Integer durationDays;

    @Column(precision = 12, scale = 2)
    private BigDecimal budgetEstimate;

    @Column(length = 50)
    private String category;

    @Column(length = 500)
    private String coverImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
