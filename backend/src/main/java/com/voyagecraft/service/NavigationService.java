package com.voyagecraft.service;

import com.voyagecraft.dto.navigation.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.TransportMode;
import com.voyagecraft.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NavigationService {

    private final NavigationRouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final TripCollaboratorRepository collaboratorRepository;

    // ── Route CRUD ───────────────────────────────────────────────────

    @Transactional
    public NavigationRouteResponse addRoute(Long tripId, NavigationRouteRequest request, User user) {
        Trip trip = getTripWithAccessCheck(tripId, user);
        TransportMode mode = TransportMode.valueOf(request.getTransportMode());

        double distance = calculateDistance(
                request.getFromLatitude(), request.getFromLongitude(),
                request.getToLatitude(), request.getToLongitude());
        int estimatedMin = estimateTravelTime(distance, mode);

        NavigationRoute route = NavigationRoute.builder()
                .trip(trip)
                .fromLocation(request.getFromLocation())
                .fromLatitude(request.getFromLatitude())
                .fromLongitude(request.getFromLongitude())
                .toLocation(request.getToLocation())
                .toLatitude(request.getToLatitude())
                .toLongitude(request.getToLongitude())
                .transportMode(mode)
                .distanceKm(Math.round(distance * 100.0) / 100.0)
                .estimatedMinutes(estimatedMin)
                .bufferMinutes(request.getBufferMinutes() != null ? request.getBufferMinutes() : getDefaultBuffer(mode))
                .directions(request.getDirections())
                .dayNumber(request.getDayNumber())
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                .build();

        return mapToResponse(routeRepository.save(route));
    }

    @Transactional
    public NavigationRouteResponse updateRoute(Long routeId, NavigationRouteRequest request, User user) {
        NavigationRoute route = getRouteWithAccessCheck(routeId, user);
        TransportMode mode = TransportMode.valueOf(request.getTransportMode());

        double distance = calculateDistance(
                request.getFromLatitude(), request.getFromLongitude(),
                request.getToLatitude(), request.getToLongitude());
        int estimatedMin = estimateTravelTime(distance, mode);

        route.setFromLocation(request.getFromLocation());
        route.setFromLatitude(request.getFromLatitude());
        route.setFromLongitude(request.getFromLongitude());
        route.setToLocation(request.getToLocation());
        route.setToLatitude(request.getToLatitude());
        route.setToLongitude(request.getToLongitude());
        route.setTransportMode(mode);
        route.setDistanceKm(Math.round(distance * 100.0) / 100.0);
        route.setEstimatedMinutes(estimatedMin);
        route.setBufferMinutes(request.getBufferMinutes() != null ? request.getBufferMinutes() : getDefaultBuffer(mode));
        route.setDirections(request.getDirections());
        route.setDayNumber(request.getDayNumber());
        route.setOrderIndex(request.getOrderIndex());

        return mapToResponse(routeRepository.save(route));
    }

    @Transactional
    public NavigationRouteResponse switchTransportMode(Long routeId, String newMode, User user) {
        NavigationRoute route = getRouteWithAccessCheck(routeId, user);
        TransportMode mode = TransportMode.valueOf(newMode);

        int estimatedMin = estimateTravelTime(route.getDistanceKm(), mode);
        route.setTransportMode(mode);
        route.setEstimatedMinutes(estimatedMin);
        route.setBufferMinutes(getDefaultBuffer(mode));

        return mapToResponse(routeRepository.save(route));
    }

    @Transactional(readOnly = true)
    public List<NavigationRouteResponse> getTripRoutes(Long tripId, User user) {
        getTripWithAccessCheck(tripId, user);
        return routeRepository.findByTripIdOrderByDayNumberAscOrderIndexAsc(tripId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NavigationRouteResponse> getDayRoutes(Long tripId, Integer dayNumber, User user) {
        getTripWithAccessCheck(tripId, user);
        return routeRepository.findByTripIdAndDayNumberOrderByOrderIndexAsc(tripId, dayNumber)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteRoute(Long routeId, User user) {
        NavigationRoute route = getRouteWithAccessCheck(routeId, user);
        routeRepository.delete(route);
    }

    // ── Day Sheets ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DaySheetResponse> getDaySheets(Long tripId, User user) {
        Trip trip = getTripWithAccessCheck(tripId, user);
        List<NavigationRoute> allRoutes = routeRepository.findByTripIdOrderByDayNumberAscOrderIndexAsc(tripId);

        Map<Integer, List<NavigationRoute>> byDay = allRoutes.stream()
                .collect(Collectors.groupingBy(r -> r.getDayNumber() != null ? r.getDayNumber() : 1, TreeMap::new, Collectors.toList()));

        List<DaySheetResponse> sheets = new ArrayList<>();
        long totalDays = ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;

        for (int day = 1; day <= totalDays; day++) {
            List<NavigationRoute> dayRoutes = byDay.getOrDefault(day, List.of());
            LocalDate date = trip.getStartDate().plusDays(day - 1);

            String destination = trip.getDestinations().isEmpty() ? "Unknown"
                    : trip.getDestinations().get(0).getDestinationName();

            int totalMin = dayRoutes.stream()
                    .mapToInt(r -> (r.getEstimatedMinutes() != null ? r.getEstimatedMinutes() : 0) + (r.getBufferMinutes() != null ? r.getBufferMinutes() : 0))
                    .sum();
            double totalDist = dayRoutes.stream()
                    .mapToDouble(r -> r.getDistanceKm() != null ? r.getDistanceKm() : 0)
                    .sum();

            String summary = dayRoutes.isEmpty()
                    ? "No navigation routes planned for this day."
                    : String.format("%d routes, %.1f km total, ~%d min travel time", dayRoutes.size(), totalDist, totalMin);

            sheets.add(DaySheetResponse.builder()
                    .dayNumber(day)
                    .date(date.toString())
                    .destination(destination)
                    .routes(dayRoutes.stream().map(this::mapToResponse).collect(Collectors.toList()))
                    .totalTravelMinutes(totalMin)
                    .totalDistanceKm(Math.round(totalDist * 100.0) / 100.0)
                    .summary(summary)
                    .build());
        }
        return sheets;
    }

    // ── Travel Time Estimation (Haversine) ───────────────────────────

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return 5.0; // default 5km
        double R = 6371; // Earth's radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1.3; // multiply by 1.3 for road vs straight-line
    }

    private int estimateTravelTime(double distanceKm, TransportMode mode) {
        double speedKmh = switch (mode) {
            case WALKING -> 5.0;
            case CYCLING -> 15.0;
            case PUBLIC_TRANSPORT -> 25.0;
            case RIDE_SHARE, TAXI -> 35.0;
            case DRIVING -> 40.0;
        };
        return (int) Math.ceil((distanceKm / speedKmh) * 60);
    }

    private int getDefaultBuffer(TransportMode mode) {
        return switch (mode) {
            case WALKING -> 5;
            case CYCLING -> 5;
            case PUBLIC_TRANSPORT -> 15; // wait for bus/train
            case RIDE_SHARE -> 10; // wait for pickup
            case TAXI -> 10;
            case DRIVING -> 5;
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private String getModeIcon(TransportMode mode) {
        return switch (mode) {
            case WALKING -> "🚶";
            case CYCLING -> "🚴";
            case PUBLIC_TRANSPORT -> "🚌";
            case RIDE_SHARE -> "🚗";
            case TAXI -> "🚕";
            case DRIVING -> "🚙";
        };
    }

    private NavigationRouteResponse mapToResponse(NavigationRoute r) {
        int buffer = r.getBufferMinutes() != null ? r.getBufferMinutes() : 0;
        int estimated = r.getEstimatedMinutes() != null ? r.getEstimatedMinutes() : 0;
        String formatted = buffer > 0
                ? estimated + " min + " + buffer + " min buffer"
                : estimated + " min";

        return NavigationRouteResponse.builder()
                .id(r.getId()).tripId(r.getTrip().getId())
                .fromLocation(r.getFromLocation()).fromLatitude(r.getFromLatitude()).fromLongitude(r.getFromLongitude())
                .toLocation(r.getToLocation()).toLatitude(r.getToLatitude()).toLongitude(r.getToLongitude())
                .transportMode(r.getTransportMode().name()).transportModeIcon(getModeIcon(r.getTransportMode()))
                .distanceKm(r.getDistanceKm()).estimatedMinutes(estimated)
                .bufferMinutes(buffer).totalMinutes(estimated + buffer)
                .directions(r.getDirections()).dayNumber(r.getDayNumber()).orderIndex(r.getOrderIndex())
                .formattedTime(formatted).build();
    }

    private Trip getTripWithAccessCheck(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new RuntimeException("Trip not found"));
        boolean isOwner = trip.getCreatedBy().getId().equals(user.getId());
        boolean isCollaborator = collaboratorRepository.findByTripIdAndUserId(trip.getId(), user.getId()).isPresent();
        if (!isOwner && !isCollaborator) throw new RuntimeException("Not authorized");
        return trip;
    }

    private NavigationRoute getRouteWithAccessCheck(Long routeId, User user) {
        NavigationRoute route = routeRepository.findById(routeId).orElseThrow(() -> new RuntimeException("Route not found"));
        getTripWithAccessCheck(route.getTrip().getId(), user);
        return route;
    }
}
