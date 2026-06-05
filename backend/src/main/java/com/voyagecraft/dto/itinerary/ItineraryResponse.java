package com.voyagecraft.dto.itinerary;

import com.voyagecraft.enums.ItineraryStatus;
import com.voyagecraft.enums.TripPace;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItineraryResponse {
    private Long id;
    private Long tripId;
    private String tripTitle;
    private String versionName;
    private Integer versionNumber;
    private ItineraryStatus status;
    private TripPace pace;
    private Boolean isActive;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ItineraryDayResponse> days;
    private Integer totalDays;
    private Integer totalActivities;
}
