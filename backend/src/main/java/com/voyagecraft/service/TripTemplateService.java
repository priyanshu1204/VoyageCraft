package com.voyagecraft.service;

import com.voyagecraft.dto.template.TemplateResponse;
import com.voyagecraft.dto.trip.TripRequest;
import com.voyagecraft.dto.trip.TripResponse;
import com.voyagecraft.entity.Trip;
import com.voyagecraft.entity.TripTemplate;
import com.voyagecraft.entity.User;
import com.voyagecraft.enums.TripPace;
import com.voyagecraft.exception.ResourceNotFoundException;
import com.voyagecraft.repository.TripTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripTemplateService {

    private final TripTemplateRepository templateRepository;
    private final TripService tripService;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<TemplateResponse> getAllTemplates() {
        return templateRepository.findAllByOrderByNameAsc().stream()
                .map(t -> modelMapper.map(t, TemplateResponse.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TemplateResponse getTemplateById(Long id) {
        TripTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        return modelMapper.map(template, TemplateResponse.class);
    }

    @Transactional
    public TripResponse createTripFromTemplate(Long templateId, LocalDate startDate, User user) {
        TripTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + templateId));

        LocalDate endDate = startDate.plusDays(template.getDurationDays() != null ? template.getDurationDays() : 7);

        TripRequest request = TripRequest.builder()
                .title(template.getName())
                .description(template.getDescription())
                .startDate(startDate)
                .endDate(endDate)
                .budgetTotal(template.getBudgetEstimate())
                .currency("USD")
                .coverImageUrl(template.getCoverImageUrl())
                .pace(TripPace.STANDARD)
                .build();

        return tripService.createTrip(request, user);
    }
}
