package com.jettech.api.solutions_clinic.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "revinfo")
@RevisionEntity(ClinicRevisionListener.class)
public class ClinicRevisionEntity extends DefaultRevisionEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_email", length = 150)
    private String userEmail;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

}
