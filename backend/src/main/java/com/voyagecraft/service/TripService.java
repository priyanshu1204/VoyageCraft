package com.voyagecraft.service;

import com.voyagecraft.dto.auth.UserResponse;
import com.voyagecraft.dto.collaborator.CollaboratorResponse;
import com.voyagecraft.dto.destination.DestinationResponse;
import com.voyagecraft.dto.trip.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.CollaboratorRole;
import com.voyagecraft.enums.InvitationStatus;
import com.voyagecraft.enums.TripPace;
import com.voyagecraft.enums.TripStatus;
import com.voyagecraft.exception.BadRequestException;
import com.voyagecraft.exception.ResourceNotFoundException;
import com.voyagecraft.exception.UnauthorizedException;
import com.voyagecraft.repository.TripCollaboratorRepository;
import com.voyagecraft.repository.TripRepository;
import com.voyagecraft.repository.WeatherForecastRepository;
import com.voyagecraft.repository.OfflineCacheRepository;
import com.voyagecraft.repository.SyncLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private final TripCollaboratorRepository collaboratorRepository;
    private final ModelMapper modelMapper;
    private final WeatherForecastRepository weatherForecastRepository;
    private final OfflineCacheRepository offlineCacheRepository;
    private final SyncLogRepository syncLogRepository;

    @Transactional
    public TripResponse createTrip(TripRequest request, User user) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        Trip trip = Trip.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budgetTotal(request.getBudgetTotal())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .coverImageUrl(request.getCoverImageUrl())
                .pace(request.getPace() != null ? request.getPace() : TripPace.STANDARD)
                .status(TripStatus.PLANNING)
                .createdBy(user)
                .build();

        // Add destinations if provided
        if (request.getDestinations() != null) {
            request.getDestinations().forEach(destReq -> {
                TripDestination dest = TripDestination.builder()
                        .trip(trip)
                        .destinationName(destReq.getDestinationName())
                        .country(destReq.getCountry())
                        .latitude(destReq.getLatitude())
                        .longitude(destReq.getLongitude())
                        .arrivalDate(destReq.getArrivalDate())
                        .departureDate(destReq.getDepartureDate())
                        .orderIndex(destReq.getOrderIndex() != null ? destReq.getOrderIndex() : 0)
                        .notes(destReq.getNotes())
                        .build();
                trip.getDestinations().add(dest);
            });
        }

        // Add owner as collaborator
        TripCollaborator ownerCollab = TripCollaborator.builder()
                .trip(trip)
                .user(user)
                .role(CollaboratorRole.OWNER)
                .invitationStatus(InvitationStatus.ACCEPTED)
                .invitedBy(user)
                .invitedAt(java.time.LocalDateTime.now())
                .respondedAt(java.time.LocalDateTime.now())
                .build();
        trip.getCollaborators().add(ownerCollab);

        Trip saved = tripRepository.save(trip);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TripResponse> getAllUserTrips(User user) {
        return tripRepository.findAllUserTrips(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TripResponse getTripById(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));
        verifyAccess(trip, user);
        return mapToResponse(trip);
    }

    @Transactional
    public TripResponse updateTrip(Long tripId, TripRequest request, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));
        verifyEditAccess(trip, user);

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        trip.setTitle(request.getTitle());
        trip.setDescription(request.getDescription());
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        if (request.getBudgetTotal() != null) trip.setBudgetTotal(request.getBudgetTotal());
        if (request.getCurrency() != null) trip.setCurrency(request.getCurrency());
        if (request.getCoverImageUrl() != null) trip.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getPace() != null) trip.setPace(request.getPace());

        Trip saved = tripRepository.save(trip);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteTrip(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));
        if (!trip.getCreatedBy().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only the trip owner can delete the trip");
        }
        // Explicitly remove orphan records that may have DB-level FK constraints
        weatherForecastRepository.deleteByTripId(tripId);
        offlineCacheRepository.deleteByTripId(tripId);
        syncLogRepository.deleteByTripId(tripId);
        tripRepository.delete(trip);
    }

    @Transactional(readOnly = true)
    public TripDashboardResponse getDashboard(User user) {
        List<Trip> allTrips = tripRepository.findAllUserTrips(user.getId());

        long planning = allTrips.stream().filter(t -> t.getStatus() == TripStatus.PLANNING).count();
        long active = allTrips.stream().filter(t ->
                t.getStatus() == TripStatus.ACTIVE ||
                (!t.getStartDate().isAfter(LocalDate.now()) && !t.getEndDate().isBefore(LocalDate.now()))
        ).count();
        long completed = allTrips.stream().filter(t -> t.getStatus() == TripStatus.COMPLETED).count();

        List<TripResponse> upcoming = allTrips.stream()
                .filter(t -> t.getStartDate().isAfter(LocalDate.now()))
                .sorted((a, b) -> a.getStartDate().compareTo(b.getStartDate()))
                .limit(5)
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<TripResponse> recent = allTrips.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return TripDashboardResponse.builder()
                .totalTrips(allTrips.size())
                .planningTrips(planning)
                .activeTrips(active)
                .completedTrips(completed)
                .upcomingTrips(upcoming)
                .recentTrips(recent)
                .build();
    }

    private void verifyAccess(Trip trip, User user) {
        boolean isOwner = trip.getCreatedBy().getId().equals(user.getId());
        boolean isCollaborator = collaboratorRepository.findByTripIdAndUserId(trip.getId(), user.getId())
                .map(c -> c.getInvitationStatus() == InvitationStatus.ACCEPTED)
                .orElse(false);
        if (!isOwner && !isCollaborator) {
            throw new UnauthorizedException("You don't have access to this trip");
        }
    }

    private void verifyEditAccess(Trip trip, User user) {
        boolean isOwner = trip.getCreatedBy().getId().equals(user.getId());
        boolean isEditor = collaboratorRepository.findByTripIdAndUserId(trip.getId(), user.getId())
                .map(c -> c.getInvitationStatus() == InvitationStatus.ACCEPTED
                        && (c.getRole() == CollaboratorRole.OWNER || c.getRole() == CollaboratorRole.EDITOR))
                .orElse(false);
        if (!isOwner && !isEditor) {
            throw new UnauthorizedException("You don't have edit access to this trip");
        }
    }

    public TripResponse mapToResponse(Trip trip) {
        int daysUntil = (int) ChronoUnit.DAYS.between(LocalDate.now(), trip.getStartDate());
        int totalDays = (int) ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;

        UserResponse createdByResp = UserResponse.builder()
                .id(trip.getCreatedBy().getId())
                .email(trip.getCreatedBy().getEmail())
                .firstName(trip.getCreatedBy().getFirstName())
                .lastName(trip.getCreatedBy().getLastName())
                .avatarUrl(trip.getCreatedBy().getAvatarUrl())
                .build();

        List<DestinationResponse> destinations = trip.getDestinations().stream()
                .map(d -> modelMapper.map(d, DestinationResponse.class))
                .collect(Collectors.toList());

        List<CollaboratorResponse> collaborators = trip.getCollaborators().stream()
                .map(c -> {
                    UserResponse userResp = UserResponse.builder()
                            .id(c.getUser().getId())
                            .email(c.getUser().getEmail())
                            .firstName(c.getUser().getFirstName())
                            .lastName(c.getUser().getLastName())
                            .avatarUrl(c.getUser().getAvatarUrl())
                            .build();
                    return CollaboratorResponse.builder()
                            .id(c.getId())
                            .user(userResp)
                            .role(c.getRole())
                            .invitationStatus(c.getInvitationStatus())
                            .invitedAt(c.getInvitedAt())
                            .respondedAt(c.getRespondedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return TripResponse.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .description(trip.getDescription())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .status(trip.getStatus())
                .budgetTotal(trip.getBudgetTotal())
                .currency(trip.getCurrency())
                .coverImageUrl(trip.getCoverImageUrl())
                .pace(trip.getPace())
                .createdBy(createdByResp)
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .destinations(destinations)
                .collaborators(collaborators)
                .daysUntilTrip(daysUntil)
                .totalDays(totalDays)
                .build();
    }
}
