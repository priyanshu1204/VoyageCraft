package com.voyagecraft.entity;

import com.voyagecraft.enums.StayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "stays")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Stay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StayType type;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String address;

    @Column(length = 200)
    private String city;

    @Column(nullable = false)
    private LocalDate checkInDate;

    private LocalTime checkInTime;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    private LocalTime checkOutTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal costPerNight;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(length = 10)
    @Builder.Default
    private String currency = "USD";

    @Column(length = 100)
    private String bookingReference;

    @Column(length = 500)
    private String bookingUrl;

    @Column(length = 200)
    private String contactPhone;

    @Column(length = 200)
    private String contactEmail;

    @Column(length = 2000)
    private String notes;

    @Column(length = 2000)
    private String cancellationPolicy;

    @Column(length = 1000)
    private String amenities;

    @Builder.Default
    private Boolean checkedIn = false;

    @Column(columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer starRating = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
