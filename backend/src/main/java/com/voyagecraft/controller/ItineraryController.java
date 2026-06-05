package com.voyagecraft.controller;

import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.dto.itinerary.*;
import com.voyagecraft.entity.User;
import com.voyagecraft.service.AuthService;
import com.voyagecraft.service.ItineraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/itineraries")
@RequiredArgsConstructor
@Tag(name = "Itineraries", description = "Auto-generated itinerary management endpoints (US-02)")
public class ItineraryController {

    private final ItineraryService itineraryService;
    private final AuthService authService;

    @PostMapping("/generate")
    @Operation(summary = "Generate a new itinerary draft for a trip",
               description = "Auto-generates an itinerary based on trip destinations, pace preference, and mock opening hours/seasons. Each generation creates a new version.")
    public ResponseEntity<ApiResponse<ItineraryResponse>> generateItinerary(
            @Valid @RequestBody GenerateItineraryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        ItineraryResponse response = itineraryService.generateItinerary(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Itinerary generated successfully", response));
    }

    @GetMapping("/trip/{tripId}/history")
    @Operation(summary = "Get version history of all itineraries for a trip",
               description = "Returns a summary list of all generated itinerary versions for the given trip, ordered by version number.")
    public ResponseEntity<ApiResponse<List<ItinerarySummaryResponse>>> getVersionHistory(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        List<ItinerarySummaryResponse> history = itineraryService.getVersionHistory(tripId, user);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/{itineraryId}")
    @Operation(summary = "Get full details of a specific itinerary version")
    public ResponseEntity<ApiResponse<ItineraryResponse>> getItinerary(
            @PathVariable Long itineraryId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        ItineraryResponse response = itineraryService.getItinerary(itineraryId, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{itineraryId}/activate")
    @Operation(summary = "Activate an itinerary version",
               description = "Sets the given itinerary as the active plan. All other versions for the trip will be archived.")
    public ResponseEntity<ApiResponse<ItineraryResponse>> activateItinerary(
            @PathVariable Long itineraryId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        ItineraryResponse response = itineraryService.activateItinerary(itineraryId, user);
        return ResponseEntity.ok(ApiResponse.success("Itinerary activated successfully", response));
    }

    @DeleteMapping("/{itineraryId}")
    @Operation(summary = "Delete an itinerary draft version",
               description = "Deletes a draft or archived itinerary version. The active version cannot be deleted.")
    public ResponseEntity<ApiResponse<Void>> deleteItinerary(
            @PathVariable Long itineraryId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        itineraryService.deleteItinerary(itineraryId, user);
        return ResponseEntity.ok(ApiResponse.success("Itinerary deleted successfully", null));
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare two itinerary versions side-by-side",
               description = "Returns a comparison of two itinerary versions including pace, activity count, and day-by-day breakdown.")
    public ResponseEntity<ApiResponse<ItineraryCompareResponse>> compareVersions(
            @RequestParam Long versionAId,
            @RequestParam Long versionBId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        ItineraryCompareResponse response = itineraryService.compareVersions(versionAId, versionBId, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
