package com.voyagecraft.entity;

import com.voyagecraft.enums.ChecklistItemType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_checklists")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DocumentChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_document_id", nullable = false)
    private TravelDocument travelDocument;

    @Column(nullable = false, length = 300)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChecklistItemType itemType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String documentLink;

    @Builder.Default
    private Boolean completed = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
