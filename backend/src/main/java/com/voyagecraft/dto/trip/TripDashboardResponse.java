package com.voyagecraft.dto.trip;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TripDashboardResponse {
    private long totalTrips;
    private long planningTrips;
    private long activeTrips;
    private long completedTrips;
    private List<TripResponse> upcomingTrips;
    private List<TripResponse> recentTrips;
}
