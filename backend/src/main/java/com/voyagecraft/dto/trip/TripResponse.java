package com.voyagecraft.dto.trip;

import com.voyagecraft.dto.auth.UserResponse;
import com.voyagecraft.dto.collaborator.CollaboratorResponse;
import com.voyagecraft.dto.destination.DestinationResponse;
import com.voyagecraft.enums.TripPace;
import com.voyagecraft.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TripResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private TripStatus status;
    private BigDecimal budgetTotal;
    private String currency;
    private String coverImageUrl;
    private TripPace pace;
    private UserResponse createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DestinationResponse> destinations;
    private List<CollaboratorResponse> collaborators;
    private int daysUntilTrip;
    private int totalDays;
}
