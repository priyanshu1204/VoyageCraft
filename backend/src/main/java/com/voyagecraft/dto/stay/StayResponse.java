package com.voyagecraft.dto.stay;

import com.voyagecraft.enums.StayType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class StayResponse {
    private Long id;
    private Long tripId;
    private StayType type;
    private String name;
    private String address;
    private String city;
    private LocalDate checkInDate;
    private LocalTime checkInTime;
    private LocalDate checkOutDate;
    private LocalTime checkOutTime;
    private BigDecimal costPerNight;
    private BigDecimal totalCost;
    private String currency;
    private String bookingReference;
    private String bookingUrl;
    private String contactPhone;
    private String contactEmail;
    private String notes;
    private String cancellationPolicy;
    private String amenities;
    private Integer starRating;
    private Long totalNights;
    private LocalDateTime createdAt;
}
