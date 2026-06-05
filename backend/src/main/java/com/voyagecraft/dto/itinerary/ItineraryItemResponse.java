package com.voyagecraft.dto.itinerary;

import com.voyagecraft.enums.ActivityCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
public class ItineraryItemResponse {
    private Long id;
    private String title;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private String locationName;
    private String locationAddress;
    private Double latitude;
    private Double longitude;
    private BigDecimal costEstimate;
    private ActivityCategory category;
    private Integer orderIndex;
    private String openingHours;
    private String bestSeason;
    private Integer durationMinutes;
}
