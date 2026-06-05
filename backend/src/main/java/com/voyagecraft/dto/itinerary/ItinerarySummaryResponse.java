package com.voyagecraft.dto.itinerary;

import com.voyagecraft.enums.ItineraryStatus;
import com.voyagecraft.enums.TripPace;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ItinerarySummaryResponse {
    private Long id;
    private String versionName;
    private Integer versionNumber;
    private ItineraryStatus status;
    private TripPace pace;
    private Boolean isActive;
    private Integer totalDays;
    private Integer totalActivities;
    private LocalDateTime createdAt;
}
