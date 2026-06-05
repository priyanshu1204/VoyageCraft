package com.voyagecraft.dto.activity;

import com.voyagecraft.enums.ActivityCategory;
import com.voyagecraft.enums.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class ActivityResponse {
    private Long id;
    private Long tripId;
    private String name;
    private String description;
    private ActivityCategory category;
    private String tags;
    private List<String> tagList;
    private String location;
    private String address;
    private LocalDate activityDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String openingHours;
    private String seasonalNotes;
    private BigDecimal cost;
    private String currency;
    private ReservationStatus reservationStatus;
    private String bookingReference;
    private String bookingUrl;
    private LocalDateTime reminderAt;
    private String notes;
    private String alternatives;
    private List<String> alternativeList;
    private Integer priority;
    private LocalDateTime createdAt;
}
