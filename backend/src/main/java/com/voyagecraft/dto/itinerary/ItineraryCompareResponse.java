package com.voyagecraft.dto.itinerary;

import com.voyagecraft.enums.ItineraryStatus;
import com.voyagecraft.enums.TripPace;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItineraryCompareResponse {
    private ItinerarySummaryResponse versionA;
    private ItinerarySummaryResponse versionB;
    private List<ItineraryDayResponse> daysA;
    private List<ItineraryDayResponse> daysB;
    private String paceComparison;
    private int activityDifference;
}
