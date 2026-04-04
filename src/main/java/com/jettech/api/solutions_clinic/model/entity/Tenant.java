package com.jettech.api.solutions_clinic.model.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Entity(name = "tenant")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String cnpj;

    @Enumerated(EnumType.STRING)
    private PlanType planType;
    private String address;
    private String phone;
    private boolean active;

    @Column(length = 64, unique = true)
    private String subdomain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTenant type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'PENDING_SETUP'")
    private TenantStatus status = TenantStatus.PENDING_SETUP;

    private LocalDate trialEndsAt;

    /** ID do modelo de prontuário pré-selecionado ao abrir novo prontuário (pode ser global ou da clínica). */
    @Column(name = "default_medical_record_template_id")
    private UUID defaultMedicalRecordTemplateId;

    /** Chave do objeto R2 com o logo da clínica (ex: tenants/{id}/logo/logo.png). */
    @Column(name = "logo_object_key", length = 512)
    private String logoObjectKey;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
