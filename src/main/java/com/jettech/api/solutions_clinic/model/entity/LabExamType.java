package com.jettech.api.solutions_clinic.model.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Entity(name = "lab_exam_types")
@Table(name = "lab_exam_types")
public class LabExamType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LabSector sector = LabSector.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(name = "sample_type", length = 50)
    private SampleType sampleType;

    @Column(length = 50)
    private String unit;

    @Column(name = "reference_range_text", columnDefinition = "TEXT")
    private String referenceRangeText;

    @Column(name = "preparation_info", columnDefinition = "TEXT")
    private String preparationInfo;

    @Column(name = "turnaround_hours")
    private Integer turnaroundHours;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
