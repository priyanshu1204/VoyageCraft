package com.voyagecraft.entity;

import com.voyagecraft.enums.PackingCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "packing_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PackingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PackingCategory category;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(nullable = false)
    @Builder.Default
    private Boolean packed = false;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isFromTemplate = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
