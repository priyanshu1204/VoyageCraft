package com.voyagecraft.dto.itinerary;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ItineraryDayResponse {
    private Long id;
    private Integer dayIndex;
    private LocalDate dayDate;
    private String theme;
    private String notes;
    private String destinationName;
    private List<ItineraryItemResponse> items;
}
