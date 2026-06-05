package com.voyagecraft.dto.navigation;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DaySheetResponse {
    private Integer dayNumber;
    private String date;
    private String destination;
    private List<NavigationRouteResponse> routes;
    private Integer totalTravelMinutes;
    private Double totalDistanceKm;
    private String summary;
}
