package com.voyagecraft.entity;

import com.voyagecraft.enums.ActivityCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "itinerary_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ItineraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_day_id", nullable = false)
    private ItineraryDay itineraryDay;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    private LocalTime startTime;

    private LocalTime endTime;

    @Column(length = 200)
    private String locationName;

    @Column(length = 500)
    private String locationAddress;

    private Double latitude;

    private Double longitude;

    @Column(precision = 10, scale = 2)
    private BigDecimal costEstimate;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private ActivityCategory category = ActivityCategory.ATTRACTION;

    @Column(nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    // Opening hours info (used during generation)
    @Column(length = 100)
    private String openingHours;

    @Column(length = 100)
    private String bestSeason;

    // Duration in minutes
    private Integer durationMinutes;
}
