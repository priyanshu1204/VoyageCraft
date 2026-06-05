package com.voyagecraft.dto.stay;

import com.voyagecraft.enums.StayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StayRequest {

    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotNull(message = "Stay type is required")
    private StayType type;

    @NotBlank(message = "Name is required")
    private String name;

    private String address;
    private String city;

    @NotBlank(message = "Check-in date is required")
    private String checkInDate; // "2026-06-01"

    private String checkInTime;  // "14:00"

    @NotBlank(message = "Check-out date is required")
    private String checkOutDate;

    private String checkOutTime; // "11:00"

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
}
