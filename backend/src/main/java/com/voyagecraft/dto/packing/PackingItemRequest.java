package com.voyagecraft.dto.packing;

import com.voyagecraft.enums.PackingCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PackingItemRequest {
    @NotNull private Long tripId;
    @NotBlank private String name;
    @NotNull private PackingCategory category;
    private Integer quantity = 1;
    private Boolean packed = false;
    private String notes;
}
