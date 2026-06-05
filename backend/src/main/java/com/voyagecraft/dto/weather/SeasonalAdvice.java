package com.voyagecraft.dto.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SeasonalAdvice {
    private String locationName;
    private String season;
    private String temperatureRange;
    private String bestTimeToVisit;
    private String advice;
    private List<String> recommendedActivities;
    private List<String> itemsToPack;
}
