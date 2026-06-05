package com.voyagecraft.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_destinations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TripDestination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false, length = 200)
    private String destinationName;

    @Column(length = 100)
    private String country;

    private Double latitude;

    private Double longitude;

    private LocalDate arrivalDate;

    private LocalDate departureDate;

    @Column(nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    @Column(length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
