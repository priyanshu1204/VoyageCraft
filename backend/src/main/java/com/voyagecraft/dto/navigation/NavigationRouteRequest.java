package com.voyagecraft.dto.navigation;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NavigationRouteRequest {
    private String fromLocation;
    private Double fromLatitude;
    private Double fromLongitude;
    private String toLocation;
    private Double toLatitude;
    private Double toLongitude;
    private String transportMode; // PUBLIC_TRANSPORT, RIDE_SHARE, WALKING, CYCLING, DRIVING, TAXI
    private Integer bufferMinutes;
    private String directions;
    private Integer dayNumber;
    private Integer orderIndex;
}
