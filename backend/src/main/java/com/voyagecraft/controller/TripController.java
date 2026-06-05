package com.voyagecraft.controller;

import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.dto.trip.*;
import com.voyagecraft.entity.User;
import com.voyagecraft.service.AuthService;
import com.voyagecraft.service.TripService;
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
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = "Trips", description = "Trip management endpoints")
public class TripController {

    private final TripService tripService;
    private final AuthService authService;

    @PostMapping
    @Operation(summary = "Create a new trip")
    public ResponseEntity<ApiResponse<TripResponse>> createTrip(
            @Valid @RequestBody TripRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        TripResponse response = tripService.createTrip(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Trip created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all trips for current user")
    public ResponseEntity<ApiResponse<List<TripResponse>>> getAllTrips(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        List<TripResponse> trips = tripService.getAllUserTrips(user);
        return ResponseEntity.ok(ApiResponse.success(trips));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trip by ID")
    public ResponseEntity<ApiResponse<TripResponse>> getTripById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        TripResponse response = tripService.getTripById(id, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update trip")
    public ResponseEntity<ApiResponse<TripResponse>> updateTrip(
            @PathVariable Long id,
            @Valid @RequestBody TripRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        TripResponse response = tripService.updateTrip(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Trip updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete trip")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        tripService.deleteTrip(id, user);
        return ResponseEntity.ok(ApiResponse.success("Trip deleted successfully", null));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard overview")
    public ResponseEntity<ApiResponse<TripDashboardResponse>> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        TripDashboardResponse dashboard = tripService.getDashboard(user);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
