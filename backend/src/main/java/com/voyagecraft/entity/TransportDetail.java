package com.voyagecraft.entity;

import com.voyagecraft.enums.TransportType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transport_details")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TransportDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransportType type;

    @Column(length = 100)
    private String provider;

    @Column(length = 50)
    private String flightNumber;

    @Column(nullable = false, length = 200)
    private String departureLocation;

    @Column(nullable = false, length = 200)
    private String arrivalLocation;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    @Column(length = 50)
    @Builder.Default
    private String departureTimezone = "UTC";

    @Column(length = 50)
    @Builder.Default
    private String arrivalTimezone = "UTC";

    @Column(length = 100)
    private String bookingReference;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(length = 1000)
    private String notes;

    @Builder.Default
    private Boolean checkedIn = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
