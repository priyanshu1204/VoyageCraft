package com.voyagecraft.service;

import com.voyagecraft.dto.destination.DestinationRequest;
import com.voyagecraft.dto.destination.DestinationResponse;
import com.voyagecraft.entity.Trip;
import com.voyagecraft.entity.TripDestination;
import com.voyagecraft.entity.User;
import com.voyagecraft.exception.ResourceNotFoundException;
import com.voyagecraft.repository.TripDestinationRepository;
import com.voyagecraft.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripDestinationService {

    private final TripDestinationRepository destinationRepository;
    private final TripRepository tripRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public DestinationResponse addDestination(Long tripId, DestinationRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        TripDestination destination = TripDestination.builder()
                .trip(trip)
                .destinationName(request.getDestinationName())
                .country(request.getCountry())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .arrivalDate(request.getArrivalDate())
                .departureDate(request.getDepartureDate())
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                .notes(request.getNotes())
                .build();

        destination = destinationRepository.save(destination);
        return modelMapper.map(destination, DestinationResponse.class);
    }

    @Transactional(readOnly = true)
    public List<DestinationResponse> getDestinations(Long tripId) {
        return destinationRepository.findByTripIdOrderByOrderIndexAsc(tripId).stream()
                .map(d -> modelMapper.map(d, DestinationResponse.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public DestinationResponse updateDestination(Long tripId, Long destId, DestinationRequest request) {
        TripDestination destination = destinationRepository.findById(destId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + destId));

        destination.setDestinationName(request.getDestinationName());
        destination.setCountry(request.getCountry());
        destination.setLatitude(request.getLatitude());
        destination.setLongitude(request.getLongitude());
        destination.setArrivalDate(request.getArrivalDate());
        destination.setDepartureDate(request.getDepartureDate());
        if (request.getOrderIndex() != null) destination.setOrderIndex(request.getOrderIndex());
        if (request.getNotes() != null) destination.setNotes(request.getNotes());

        destination = destinationRepository.save(destination);
        return modelMapper.map(destination, DestinationResponse.class);
    }

    @Transactional
    public void deleteDestination(Long tripId, Long destId) {
        TripDestination destination = destinationRepository.findById(destId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + destId));
        destinationRepository.delete(destination);
    }
}
