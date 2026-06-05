package com.voyagecraft.dto.packing;

import com.voyagecraft.enums.PackingCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PackingItemResponse {
    private Long id;
    private Long tripId;
    private String name;
    private PackingCategory category;
    private Integer quantity;
    private Boolean packed;
    private String notes;
    private Boolean isFromTemplate;
    private LocalDateTime createdAt;
}
