package com.voyagecraft.controller;

import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.dto.weather.*;
import com.voyagecraft.entity.User;
import com.voyagecraft.service.AuthService;
import com.voyagecraft.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@Tag(name = "Weather", description = "Weather-Aware Planning (US-10)")
public class WeatherController {

    private final WeatherService weatherService;
    private final AuthService authService;

    @PostMapping("/generate/{tripId}")
    @Operation(summary = "Generate mock weather forecasts for all trip destinations")
    public ResponseEntity<ApiResponse<List<WeatherForecastResponse>>> generateForecast(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Forecasts generated", weatherService.generateForecast(tripId, user)));
    }

    @GetMapping("/trip/{tripId}")
    @Operation(summary = "Get all weather forecasts for a trip")
    public ResponseEntity<ApiResponse<List<WeatherForecastResponse>>> getTripForecasts(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(weatherService.getTripForecasts(tripId, user)));
    }

    @GetMapping("/trip/{tripId}/date/{date}")
    @Operation(summary = "Get forecasts for a specific date")
    public ResponseEntity<ApiResponse<List<WeatherForecastResponse>>> getForecastByDate(
            @PathVariable Long tripId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(weatherService.getForecastByDate(tripId, date, user)));
    }

    @GetMapping("/alerts/{tripId}")
    @Operation(summary = "Get weather alerts with recommended activity swaps")
    public ResponseEntity<ApiResponse<List<WeatherAlertResponse>>> getAlerts(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(weatherService.getWeatherAlerts(tripId, user)));
    }

    @GetMapping("/seasonal/{tripId}")
    @Operation(summary = "Get seasonal advice for all trip destinations")
    public ResponseEntity<ApiResponse<List<SeasonalAdvice>>> getSeasonalAdvice(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(weatherService.getTripSeasonalAdvice(tripId, user)));
    }

    @GetMapping("/seasonal/location")
    @Operation(summary = "Get seasonal advice for a specific location and date")
    public ResponseEntity<ApiResponse<SeasonalAdvice>> getLocationAdvice(
            @RequestParam String location,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(weatherService.getSeasonalAdvice(location, date)));
    }
}
