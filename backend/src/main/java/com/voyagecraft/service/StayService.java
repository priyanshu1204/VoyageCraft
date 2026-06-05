package com.voyagecraft.service;

import com.voyagecraft.dto.stay.StayRequest;
import com.voyagecraft.dto.stay.StayResponse;
import com.voyagecraft.entity.Stay;
import com.voyagecraft.entity.Trip;
import com.voyagecraft.entity.User;
import com.voyagecraft.exception.BadRequestException;
import com.voyagecraft.exception.ResourceNotFoundException;
import com.voyagecraft.exception.UnauthorizedException;
import com.voyagecraft.repository.StayRepository;
import com.voyagecraft.repository.TripCollaboratorRepository;
import com.voyagecraft.repository.TripRepository;
import com.voyagecraft.enums.CollaboratorRole;
import com.voyagecraft.enums.InvitationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StayService {

    private final StayRepository stayRepository;
    private final TripRepository tripRepository;
    private final TripCollaboratorRepository collaboratorRepository;

    @Transactional
    public StayResponse addStay(StayRequest request, User user) {
        Trip trip = getTripWithOwnerCheck(request.getTripId(), user);

        LocalDate checkIn = LocalDate.parse(request.getCheckInDate());
        LocalDate checkOut = LocalDate.parse(request.getCheckOutDate());

        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("Check-out date must be after check-in date");
        }

        Stay stay = Stay.builder()
                .trip(trip)
                .type(request.getType())
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .checkInDate(checkIn)
                .checkInTime(request.getCheckInTime() != null ? LocalTime.parse(request.getCheckInTime()) : null)
                .checkOutDate(checkOut)
                .checkOutTime(request.getCheckOutTime() != null ? LocalTime.parse(request.getCheckOutTime()) : null)
                .costPerNight(request.getCostPerNight())
                .totalCost(request.getTotalCost())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .bookingReference(request.getBookingReference())
                .bookingUrl(request.getBookingUrl())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .notes(request.getNotes())
                .cancellationPolicy(request.getCancellationPolicy())
                .amenities(request.getAmenities())
                .starRating(request.getStarRating() != null ? request.getStarRating() : 0)
                .build();

        Stay saved = stayRepository.save(stay);
        log.info("Added {} stay '{}' for trip ID={}", request.getType(), request.getName(), trip.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public StayResponse updateStay(Long stayId, StayRequest request, User user) {
        Stay stay = stayRepository.findById(stayId)
                .orElseThrow(() -> new ResourceNotFoundException("Stay not found: " + stayId));
        getTripWithOwnerCheck(stay.getTrip().getId(), user);

        LocalDate checkIn = LocalDate.parse(request.getCheckInDate());
        LocalDate checkOut = LocalDate.parse(request.getCheckOutDate());

        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("Check-out date must be after check-in date");
        }

        stay.setType(request.getType());
        stay.setName(request.getName());
        stay.setAddress(request.getAddress());
        stay.setCity(request.getCity());
        stay.setCheckInDate(checkIn);
        stay.setCheckInTime(request.getCheckInTime() != null ? LocalTime.parse(request.getCheckInTime()) : null);
        stay.setCheckOutDate(checkOut);
        stay.setCheckOutTime(request.getCheckOutTime() != null ? LocalTime.parse(request.getCheckOutTime()) : null);
        stay.setCostPerNight(request.getCostPerNight());
        stay.setTotalCost(request.getTotalCost());
        stay.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        stay.setBookingReference(request.getBookingReference());
        stay.setBookingUrl(request.getBookingUrl());
        stay.setContactPhone(request.getContactPhone());
        stay.setContactEmail(request.getContactEmail());
        stay.setNotes(request.getNotes());
        stay.setCancellationPolicy(request.getCancellationPolicy());
        stay.setAmenities(request.getAmenities());
        stay.setStarRating(request.getStarRating() != null ? request.getStarRating() : 0);

        return mapToResponse(stayRepository.save(stay));
    }

    @Transactional
    public void deleteStay(Long stayId, User user) {
        Stay stay = stayRepository.findById(stayId)
                .orElseThrow(() -> new ResourceNotFoundException("Stay not found: " + stayId));
        getTripWithOwnerCheck(stay.getTrip().getId(), user);
        stayRepository.delete(stay);
    }

    @Transactional(readOnly = true)
    public List<StayResponse> getTripStays(Long tripId, User user) {
        getTripWithOwnerCheck(tripId, user);
        return stayRepository.findByTripIdOrderByCheckInDateAsc(tripId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StayResponse getStay(Long stayId, User user) {
        Stay stay = stayRepository.findById(stayId)
                .orElseThrow(() -> new ResourceNotFoundException("Stay not found: " + stayId));
        getTripWithOwnerCheck(stay.getTrip().getId(), user);
        return mapToResponse(stay);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Trip getTripWithOwnerCheck(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        boolean isOwner = trip.getCreatedBy().getId().equals(user.getId());
        boolean isEditor = collaboratorRepository.findByTripIdAndUserId(tripId, user.getId())
                .map(c -> c.getInvitationStatus() == InvitationStatus.ACCEPTED
                        && (c.getRole() == CollaboratorRole.OWNER || c.getRole() == CollaboratorRole.EDITOR))
                .orElse(false);
        if (!isOwner && !isEditor) {
            throw new UnauthorizedException("Access denied to this trip");
        }
        return trip;
    }

    private StayResponse mapToResponse(Stay s) {
        long nights = ChronoUnit.DAYS.between(s.getCheckInDate(), s.getCheckOutDate());
        return StayResponse.builder()
                .id(s.getId())
                .tripId(s.getTrip().getId())
                .type(s.getType())
                .name(s.getName())
                .address(s.getAddress())
                .city(s.getCity())
                .checkInDate(s.getCheckInDate())
                .checkInTime(s.getCheckInTime())
                .checkOutDate(s.getCheckOutDate())
                .checkOutTime(s.getCheckOutTime())
                .costPerNight(s.getCostPerNight())
                .totalCost(s.getTotalCost())
                .currency(s.getCurrency())
                .bookingReference(s.getBookingReference())
                .bookingUrl(s.getBookingUrl())
                .contactPhone(s.getContactPhone())
                .contactEmail(s.getContactEmail())
                .notes(s.getNotes())
                .cancellationPolicy(s.getCancellationPolicy())
                .amenities(s.getAmenities())
                .starRating(s.getStarRating())
                .totalNights(nights)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
