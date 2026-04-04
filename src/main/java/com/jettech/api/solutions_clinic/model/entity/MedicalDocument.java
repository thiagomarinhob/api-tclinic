package com.jettech.api.solutions_clinic.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_documents")
@Data
@NoArgsConstructor
public class MedicalDocument {
    
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "document_url", nullable = false)
    private String documentUrl;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "source")
    private String source;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", insertable = false, updatable = false)
    private Appointment appointment;
}
