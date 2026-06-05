package com.voyagecraft.entity;

import com.voyagecraft.enums.ActivityCategory;
import com.voyagecraft.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "activities")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityCategory category;

    @Column(length = 500)
    private String tags; // comma-separated: "family,outdoor,adventure"

    @Column(length = 200)
    private String location;

    @Column(length = 500)
    private String address;

    private LocalDate activityDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Column(length = 200)
    private String openingHours; // e.g. "9:00 AM - 6:00 PM"

    @Column(length = 500)
    private String seasonalNotes; // e.g. "Closed during monsoon season"

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(length = 10)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ReservationStatus reservationStatus = ReservationStatus.PENDING;

    @Column(length = 100)
    private String bookingReference;

    @Column(length = 500)
    private String bookingUrl;

    private LocalDateTime reminderAt;

    @Column(length = 2000)
    private String notes;

    @Column(length = 2000)
    private String alternatives; // comma-separated alternative activity names

    @Column(columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer priority = 0; // 0=normal, 1=high, 2=must-do

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
