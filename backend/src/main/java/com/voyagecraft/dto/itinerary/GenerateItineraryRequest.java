package com.voyagecraft.dto.itinerary;

import com.voyagecraft.enums.TripPace;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateItineraryRequest {

    @NotNull(message = "Please provide a valid tripId")
    private Long tripId;

    @NotNull(message = "Please provide a valid pace")
    private TripPace pace;

    // Optional version name, defaults to "Version N"
    private String versionName;

    // Optional: preferred activity categories (e.g. ["ADVENTURE", "FOOD"])
    private java.util.List<String> preferredCategories;
}
