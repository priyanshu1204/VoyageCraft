package com.voyagecraft.service;

import com.voyagecraft.dto.packing.*;
import com.voyagecraft.entity.PackingItem;
import com.voyagecraft.entity.Trip;
import com.voyagecraft.entity.User;
import com.voyagecraft.enums.ClimateType;
import com.voyagecraft.enums.CollaboratorRole;
import com.voyagecraft.enums.InvitationStatus;
import com.voyagecraft.enums.PackingCategory;
import com.voyagecraft.exception.ResourceNotFoundException;
import com.voyagecraft.exception.UnauthorizedException;
import com.voyagecraft.repository.PackingItemRepository;
import com.voyagecraft.repository.TripCollaboratorRepository;
import com.voyagecraft.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackingService {

    private final PackingItemRepository packingItemRepository;
    private final TripRepository tripRepository;
    private final TripCollaboratorRepository collaboratorRepository;
    private final com.voyagecraft.repository.UserDocumentRepository userDocumentRepository;

    // ── CRUD ────────────────────────────────────────────────────────────

    @Transactional
    public PackingItemResponse addItem(PackingItemRequest request, User user) {
        Trip trip = getTripWithAccessCheck(request.getTripId(), user);
        PackingItem item = PackingItem.builder()
                .trip(trip)
                .name(request.getName())
                .category(request.getCategory())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .packed(request.getPacked() != null ? request.getPacked() : false)
                .notes(request.getNotes())
                .isFromTemplate(false)
                .build();
        return mapToResponse(packingItemRepository.save(item));
    }

    @Transactional
    public PackingItemResponse updateItem(Long itemId, PackingItemRequest request, User user) {
        PackingItem item = packingItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Packing item not found: " + itemId));
        getTripWithAccessCheck(item.getTrip().getId(), user);

        item.setName(request.getName());
        item.setCategory(request.getCategory());
        item.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
        item.setPacked(request.getPacked() != null ? request.getPacked() : false);
        item.setNotes(request.getNotes());
        return mapToResponse(packingItemRepository.save(item));
    }

    @Transactional
    public PackingItemResponse togglePacked(Long itemId, User user) {
        PackingItem item = packingItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Packing item not found: " + itemId));
        getTripWithAccessCheck(item.getTrip().getId(), user);
        item.setPacked(!item.getPacked());
        return mapToResponse(packingItemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long itemId, User user) {
        PackingItem item = packingItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Packing item not found: " + itemId));
        getTripWithAccessCheck(item.getTrip().getId(), user);
        packingItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public List<PackingItemResponse> getTripItems(Long tripId, User user) {
        getTripWithAccessCheck(tripId, user);
        return packingItemRepository.findByTripIdOrderByCategoryAscNameAsc(tripId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PackingSummaryResponse getSummary(Long tripId, User user) {
        getTripWithAccessCheck(tripId, user);
        List<PackingItem> items = packingItemRepository.findByTripIdOrderByCategoryAscNameAsc(tripId);
        long total = items.size();
        long packed = items.stream().filter(PackingItem::getPacked).count();

        Map<PackingCategory, List<PackingItem>> byCat = items.stream().collect(Collectors.groupingBy(PackingItem::getCategory));
        List<PackingSummaryResponse.CategorySummary> breakdown = byCat.entrySet().stream()
                .map(e -> PackingSummaryResponse.CategorySummary.builder()
                        .category(e.getKey())
                        .total(e.getValue().size())
                        .packed(e.getValue().stream().filter(PackingItem::getPacked).count())
                        .build())
                .sorted(Comparator.comparing(s -> s.getCategory().name()))
                .collect(Collectors.toList());

        return PackingSummaryResponse.builder()
                .tripId(tripId).totalItems(total).packedItems(packed)
                .packedPercent(total > 0 ? (double) packed / total * 100 : 0)
                .categoryBreakdown(breakdown).build();
    }

    // ── User Documents ──────────────────────────────────────────────────

    @Transactional
    public TravelDocumentResponse addDocument(TravelDocumentRequest request, User user) {
        Trip trip = getTripWithAccessCheck(request.getTripId(), user);
        com.voyagecraft.entity.UserDocument doc = com.voyagecraft.entity.UserDocument.builder()
                .trip(trip)
                .user(user)
                .documentType(request.getDocumentType())
                .title(request.getTitle())
                .documentNumber(request.getDocumentNumber())
                .issuingCountry(request.getIssuingCountry())
                .issueDate(request.getIssueDate() != null && !request.getIssueDate().isEmpty() ? java.time.LocalDate.parse(request.getIssueDate()) : null)
                .expiryDate(request.getExpiryDate() != null && !request.getExpiryDate().isEmpty() ? java.time.LocalDate.parse(request.getExpiryDate()) : null)
                .fileUrl(request.getFileUrl())
                .notes(request.getNotes())
                .build();
        return mapDocToResponse(userDocumentRepository.save(doc));
    }

    @Transactional
    public TravelDocumentResponse updateDocument(Long docId, TravelDocumentRequest request, User user) {
        com.voyagecraft.entity.UserDocument doc = userDocumentRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));
        getTripWithAccessCheck(doc.getTrip().getId(), user);

        doc.setDocumentType(request.getDocumentType());
        doc.setTitle(request.getTitle());
        doc.setDocumentNumber(request.getDocumentNumber());
        doc.setIssuingCountry(request.getIssuingCountry());
        doc.setIssueDate(request.getIssueDate() != null && !request.getIssueDate().isEmpty() ? java.time.LocalDate.parse(request.getIssueDate()) : null);
        doc.setExpiryDate(request.getExpiryDate() != null && !request.getExpiryDate().isEmpty() ? java.time.LocalDate.parse(request.getExpiryDate()) : null);
        doc.setFileUrl(request.getFileUrl());
        doc.setNotes(request.getNotes());
        return mapDocToResponse(userDocumentRepository.save(doc));
    }

    @Transactional
    public void deleteDocument(Long docId, User user) {
        com.voyagecraft.entity.UserDocument doc = userDocumentRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));
        getTripWithAccessCheck(doc.getTrip().getId(), user);
        userDocumentRepository.delete(doc);
    }

    @Transactional(readOnly = true)
    public List<TravelDocumentResponse> getTripDocuments(Long tripId, User user) {
        getTripWithAccessCheck(tripId, user);
        return userDocumentRepository.findByTripIdOrderByExpiryDateAsc(tripId).stream()
                .map(this::mapDocToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TravelDocumentResponse> getExpiringByTrip(Long tripId, User user) {
        getTripWithAccessCheck(tripId, user);
        java.time.LocalDate threshold = java.time.LocalDate.now().plusDays(30);
        return userDocumentRepository.findExpiringByTrip(tripId, threshold).stream()
                .map(this::mapDocToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TravelDocumentResponse> getMyExpiringDocuments(User user) {
        java.time.LocalDate threshold = java.time.LocalDate.now().plusDays(30);
        return userDocumentRepository.findExpiringByUser(user.getId(), threshold).stream()
                .map(this::mapDocToResponse).collect(Collectors.toList());
    }

    // ── Template Generation ─────────────────────────────────────────────

    @Transactional
    public List<PackingItemResponse> applyTemplate(Long tripId, ClimateType climate, User user) {
        Trip trip = getTripWithAccessCheck(tripId, user);
        List<PackingTemplateItem> templateItems = getTemplateItems(climate);

        List<PackingItem> saved = new ArrayList<>();
        for (PackingTemplateItem t : templateItems) {
            PackingItem item = PackingItem.builder()
                    .trip(trip).name(t.getName()).category(t.getCategory())
                    .quantity(t.getQuantity()).packed(false).isFromTemplate(true).build();
            saved.add(packingItemRepository.save(item));
        }
        log.info("Applied {} template ({} items) to trip ID={}", climate, saved.size(), tripId);
        return saved.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<PackingTemplateItem> getTemplateItems(ClimateType climate) {
        List<PackingTemplateItem> items = new ArrayList<>();

        // Universal essentials
        items.add(PackingTemplateItem.builder().name("Passport").category(PackingCategory.DOCUMENTS).quantity(1).build());
        items.add(PackingTemplateItem.builder().name("Travel Insurance Docs").category(PackingCategory.DOCUMENTS).quantity(1).build());
        items.add(PackingTemplateItem.builder().name("Phone Charger").category(PackingCategory.ELECTRONICS).quantity(1).build());
        items.add(PackingTemplateItem.builder().name("Power Bank").category(PackingCategory.ELECTRONICS).quantity(1).build());
        items.add(PackingTemplateItem.builder().name("Headphones").category(PackingCategory.ELECTRONICS).quantity(1).build());
        items.add(PackingTemplateItem.builder().name("Toothbrush & Toothpaste").category(PackingCategory.TOILETRIES).quantity(1).build());
        items.add(PackingTemplateItem.builder().name("Deodorant").category(PackingCategory.TOILETRIES).quantity(1).build());
        items.add(PackingTemplateItem.builder().name("Shampoo (travel size)").category(PackingCategory.TOILETRIES).quantity(1).build());
        items.add(PackingTemplateItem.builder().name("Underwear").category(PackingCategory.CLOTHING).quantity(5).build());
        items.add(PackingTemplateItem.builder().name("Socks").category(PackingCategory.CLOTHING).quantity(5).build());
        items.add(PackingTemplateItem.builder().name("First Aid Kit").category(PackingCategory.HEALTH).quantity(1).build());
        items.add(PackingTemplateItem.builder().name("Medications").category(PackingCategory.HEALTH).quantity(1).build());

        // Climate-specific items
        switch (climate) {
            case TROPICAL, BEACH -> {
                items.add(PackingTemplateItem.builder().name("Sunscreen SPF 50").category(PackingCategory.HEALTH).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Sunglasses").category(PackingCategory.ACCESSORIES).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Swimsuit").category(PackingCategory.CLOTHING).quantity(2).build());
                items.add(PackingTemplateItem.builder().name("Flip Flops").category(PackingCategory.FOOTWEAR).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Beach Towel").category(PackingCategory.ACCESSORIES).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("T-Shirts").category(PackingCategory.CLOTHING).quantity(5).build());
                items.add(PackingTemplateItem.builder().name("Shorts").category(PackingCategory.CLOTHING).quantity(3).build());
                items.add(PackingTemplateItem.builder().name("Hat / Cap").category(PackingCategory.ACCESSORIES).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Insect Repellent").category(PackingCategory.HEALTH).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Waterproof Phone Pouch").category(PackingCategory.ELECTRONICS).quantity(1).build());
            }
            case COLD, MOUNTAIN -> {
                items.add(PackingTemplateItem.builder().name("Winter Jacket").category(PackingCategory.CLOTHING).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Thermal Underwear").category(PackingCategory.CLOTHING).quantity(2).build());
                items.add(PackingTemplateItem.builder().name("Warm Sweaters").category(PackingCategory.CLOTHING).quantity(3).build());
                items.add(PackingTemplateItem.builder().name("Wool Socks").category(PackingCategory.CLOTHING).quantity(4).build());
                items.add(PackingTemplateItem.builder().name("Gloves").category(PackingCategory.ACCESSORIES).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Beanie / Warm Hat").category(PackingCategory.ACCESSORIES).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Scarf").category(PackingCategory.ACCESSORIES).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Winter Boots").category(PackingCategory.FOOTWEAR).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Lip Balm").category(PackingCategory.TOILETRIES).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Hand Warmers").category(PackingCategory.ACCESSORIES).quantity(2).build());
            }
            case TEMPERATE -> {
                items.add(PackingTemplateItem.builder().name("Light Jacket").category(PackingCategory.CLOTHING).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Long-sleeve Shirts").category(PackingCategory.CLOTHING).quantity(3).build());
                items.add(PackingTemplateItem.builder().name("T-Shirts").category(PackingCategory.CLOTHING).quantity(3).build());
                items.add(PackingTemplateItem.builder().name("Jeans / Trousers").category(PackingCategory.CLOTHING).quantity(2).build());
                items.add(PackingTemplateItem.builder().name("Sneakers").category(PackingCategory.FOOTWEAR).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Umbrella (compact)").category(PackingCategory.ACCESSORIES).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Sunglasses").category(PackingCategory.ACCESSORIES).quantity(1).build());
            }
            case DESERT -> {
                items.add(PackingTemplateItem.builder().name("Loose-fit Linen Shirts").category(PackingCategory.CLOTHING).quantity(4).build());
                items.add(PackingTemplateItem.builder().name("Light Trousers").category(PackingCategory.CLOTHING).quantity(3).build());
                items.add(PackingTemplateItem.builder().name("Wide-brim Hat").category(PackingCategory.ACCESSORIES).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Sunglasses (UV Protection)").category(PackingCategory.ACCESSORIES).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Sunscreen SPF 50+").category(PackingCategory.HEALTH).quantity(2).build());
                items.add(PackingTemplateItem.builder().name("Reusable Water Bottle").category(PackingCategory.ACCESSORIES).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Sandals").category(PackingCategory.FOOTWEAR).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Scarf / Bandana").category(PackingCategory.ACCESSORIES).quantity(1).build());
            }
            case URBAN -> {
                items.add(PackingTemplateItem.builder().name("Smart Casual Shirts").category(PackingCategory.CLOTHING).quantity(4).build());
                items.add(PackingTemplateItem.builder().name("Jeans / Chinos").category(PackingCategory.CLOTHING).quantity(2).build());
                items.add(PackingTemplateItem.builder().name("Comfortable Walking Shoes").category(PackingCategory.FOOTWEAR).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Dress Shoes (optional)").category(PackingCategory.FOOTWEAR).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Daypack / Backpack").category(PackingCategory.ACCESSORIES).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Portable WiFi / SIM Card").category(PackingCategory.ELECTRONICS).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Travel Adapter").category(PackingCategory.ELECTRONICS).quantity(1).build());
            }
            case RAINY -> {
                items.add(PackingTemplateItem.builder().name("Waterproof Jacket").category(PackingCategory.CLOTHING).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Umbrella (sturdy)").category(PackingCategory.ACCESSORIES).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Waterproof Boots").category(PackingCategory.FOOTWEAR).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Quick-dry Trousers").category(PackingCategory.CLOTHING).quantity(2).build());
                items.add(PackingTemplateItem.builder().name("Quick-dry T-Shirts").category(PackingCategory.CLOTHING).quantity(4).build());
                items.add(PackingTemplateItem.builder().name("Waterproof Bag Cover").category(PackingCategory.ACCESSORIES).quantity(1).build());
                items.add(PackingTemplateItem.builder().name("Dry Bags for Electronics").category(PackingCategory.ELECTRONICS).quantity(1).build());
            }
        }

        return items;
    }

    public List<String> getAvailableClimates() {
        return Arrays.stream(ClimateType.values()).map(Enum::name).collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Trip getTripWithAccessCheck(Long tripId, User user) {
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

    private PackingItemResponse mapToResponse(PackingItem item) {
        return PackingItemResponse.builder()
                .id(item.getId()).tripId(item.getTrip().getId())
                .name(item.getName()).category(item.getCategory())
                .quantity(item.getQuantity()).packed(item.getPacked())
                .notes(item.getNotes()).isFromTemplate(item.getIsFromTemplate())
                .createdAt(item.getCreatedAt()).build();
    }

    private TravelDocumentResponse mapDocToResponse(com.voyagecraft.entity.UserDocument doc) {
        boolean isExpired = false;
        boolean isExpiringSoon = false;
        Long daysUntilExpiry = null;

        if (doc.getExpiryDate() != null) {
            java.time.LocalDate now = java.time.LocalDate.now();
            daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(now, doc.getExpiryDate());
            isExpired = daysUntilExpiry < 0;
            isExpiringSoon = daysUntilExpiry >= 0 && daysUntilExpiry <= 30;
        }

        return TravelDocumentResponse.builder()
                .id(doc.getId())
                .tripId(doc.getTrip().getId())
                .userId(doc.getUser().getId())
                .userName(doc.getUser().getFirstName() + " " + doc.getUser().getLastName())
                .documentType(doc.getDocumentType())
                .title(doc.getTitle())
                .documentNumber(doc.getDocumentNumber())
                .issuingCountry(doc.getIssuingCountry())
                .issueDate(doc.getIssueDate())
                .expiryDate(doc.getExpiryDate())
                .fileUrl(doc.getFileUrl())
                .notes(doc.getNotes())
                .isExpired(isExpired)
                .isExpiringSoon(isExpiringSoon)
                .daysUntilExpiry(daysUntilExpiry)
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
