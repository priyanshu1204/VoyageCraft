package com.voyagecraft.entity;

import com.voyagecraft.enums.TransportMode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "navigation_routes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NavigationRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false, length = 200)
    private String fromLocation;

    private Double fromLatitude;
    private Double fromLongitude;

    @Column(nullable = false, length = 200)
    private String toLocation;

    private Double toLatitude;
    private Double toLongitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_mode", nullable = false, length = 30)
    private TransportMode transportMode;

    private Double distanceKm;

    private Integer estimatedMinutes;

    private Integer bufferMinutes;

    @Column(length = 500)
    private String directions;

    private Integer dayNumber;

    private Integer orderIndex;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
