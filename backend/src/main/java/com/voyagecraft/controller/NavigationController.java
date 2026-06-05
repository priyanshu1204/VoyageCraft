package com.voyagecraft.controller;

import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.dto.navigation.*;
import com.voyagecraft.entity.User;
import com.voyagecraft.service.AuthService;
import com.voyagecraft.service.NavigationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/navigation")
@RequiredArgsConstructor
@Tag(name = "Navigation", description = "Navigation Aids (US-12)")
public class NavigationController {

    private final NavigationService navigationService;
    private final AuthService authService;

    @PostMapping("/trip/{tripId}")
    @Operation(summary = "Add a navigation route to a trip")
    public ResponseEntity<ApiResponse<NavigationRouteResponse>> addRoute(
            @PathVariable Long tripId, @RequestBody NavigationRouteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Route added", navigationService.addRoute(tripId, request, user)));
    }

    @PutMapping("/{routeId}")
    @Operation(summary = "Update a navigation route")
    public ResponseEntity<ApiResponse<NavigationRouteResponse>> updateRoute(
            @PathVariable Long routeId, @RequestBody NavigationRouteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Updated", navigationService.updateRoute(routeId, request, user)));
    }

    @PatchMapping("/{routeId}/mode/{mode}")
    @Operation(summary = "Switch transport mode for a route (recalculates time)")
    public ResponseEntity<ApiResponse<NavigationRouteResponse>> switchMode(
            @PathVariable Long routeId, @PathVariable String mode,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Mode switched", navigationService.switchTransportMode(routeId, mode, user)));
    }

    @GetMapping("/trip/{tripId}")
    @Operation(summary = "Get all navigation routes for a trip")
    public ResponseEntity<ApiResponse<List<NavigationRouteResponse>>> getTripRoutes(
            @PathVariable Long tripId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(navigationService.getTripRoutes(tripId, user)));
    }

    @GetMapping("/trip/{tripId}/day/{dayNumber}")
    @Operation(summary = "Get routes for a specific day")
    public ResponseEntity<ApiResponse<List<NavigationRouteResponse>>> getDayRoutes(
            @PathVariable Long tripId, @PathVariable Integer dayNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(navigationService.getDayRoutes(tripId, dayNumber, user)));
    }

    @DeleteMapping("/{routeId}")
    @Operation(summary = "Delete a navigation route")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(
            @PathVariable Long routeId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        navigationService.deleteRoute(routeId, user);
        return ResponseEntity.ok(ApiResponse.success("Deleted", null));
    }

    @GetMapping("/trip/{tripId}/daysheets")
    @Operation(summary = "Get printable day sheets for all trip days")
    public ResponseEntity<ApiResponse<List<DaySheetResponse>>> getDaySheets(
            @PathVariable Long tripId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(navigationService.getDaySheets(tripId, user)));
    }
}
