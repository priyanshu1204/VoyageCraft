package com.voyagecraft.dto.activity;

import com.voyagecraft.enums.ActivityCategory;
import com.voyagecraft.enums.ReservationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActivityRequest {

    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotBlank(message = "Activity name is required")
    private String name;

    private String description;

    @NotNull(message = "Category is required")
    private ActivityCategory category;

    private String tags; // "family,outdoor,adventure"
    private String location;
    private String address;
    private String activityDate; // "2026-06-03"
    private String startTime;   // "09:00"
    private String endTime;     // "12:00"
    private String openingHours;
    private String seasonalNotes;
    private BigDecimal cost;
    private String currency;
    private ReservationStatus reservationStatus;
    private String bookingReference;
    private String bookingUrl;
    private String reminderAt; // "2026-06-01T10:00:00"
    private String notes;
    private String alternatives;
    private Integer priority;
}
