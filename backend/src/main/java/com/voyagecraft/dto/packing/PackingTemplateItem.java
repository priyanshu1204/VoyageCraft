package com.voyagecraft.dto.packing;

import com.voyagecraft.enums.PackingCategory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PackingTemplateItem {
    private String name;
    private PackingCategory category;
    private Integer quantity;
}
