package com.voyagecraft.dto.navigation;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NavigationRouteResponse {
    private Long id;
    private Long tripId;
    private String fromLocation;
    private Double fromLatitude;
    private Double fromLongitude;
    private String toLocation;
    private Double toLatitude;
    private Double toLongitude;
    private String transportMode;
    private String transportModeIcon;
    private Double distanceKm;
    private Integer estimatedMinutes;
    private Integer bufferMinutes;
    private Integer totalMinutes; // estimated + buffer
    private String directions;
    private Integer dayNumber;
    private Integer orderIndex;
    private String formattedTime; // e.g. "25 min + 10 min buffer"
}
