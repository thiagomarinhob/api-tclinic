package com.jettech.api.solutions_clinic.model.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Audited
@Entity(name = "professionals")
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "uk_professional_user_tenant", columnNames = {"user_id", "tenant_id"})
})
public class Professional {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Specialty specialty;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private DocumentType documentType;

    private String documentNumber;

    private String documentState;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}