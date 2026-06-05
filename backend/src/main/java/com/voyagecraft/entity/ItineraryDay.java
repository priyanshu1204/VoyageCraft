package com.voyagecraft.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "itinerary_days")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ItineraryDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private TripDestination destination;

    @Column(nullable = false)
    private LocalDate dayDate;

    @Column(nullable = false)
    private Integer dayIndex;

    @Column(length = 100)
    private String theme;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "itineraryDay", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<ItineraryItem> items = new ArrayList<>();
}
