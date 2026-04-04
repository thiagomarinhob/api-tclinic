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
@Entity(name = "lab_order_items")
@Table(name = "lab_order_items")
public class LabOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private LabOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_type_id")
    private LabExamType examType;

    @Column(name = "exam_name", nullable = false)
    private String examName;

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

    @Column(name = "result_value", columnDefinition = "TEXT")
    private String resultValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_status", nullable = false, length = 30)
    private LabResultStatus resultStatus = LabResultStatus.PENDING;

    @Column(name = "is_abnormal")
    private Boolean abnormal;

    @Column(name = "is_critical")
    private boolean critical = false;

    @Column(name = "technical_validated_at")
    private LocalDateTime technicalValidatedAt;

    @Column(name = "technical_validated_by")
    private String technicalValidatedBy;

    @Column(name = "final_validated_at")
    private LocalDateTime finalValidatedAt;

    @Column(name = "final_validated_by")
    private String finalValidatedBy;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
