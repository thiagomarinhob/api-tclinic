package com.jettech.api.solutions_clinic.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
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
@Entity(name = "patients")
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "uk_patient_cpf_tenant", columnNames = {"cpf", "tenant_id"})
})
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String firstName;

    @Column(unique = false)
    @Pattern(regexp = "\\d{11}", message = "O campo [cpf] deve conter exatamente 11 dígitos")
    private String cpf;

    @Column(length = 10)
    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "O campo [birthDate] deve ser uma data válida no formato DD/MM/YYYY")
    private String birthDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Email(message = "O campo [email] deve ser um email válido")
    private String email;

    private String phone;
    private String whatsapp;

    private String addressStreet;
    private String addressNumber;
    private String addressComplement;
    private String addressNeighborhood;
    private String addressCity;
    private String addressState;
    private String addressZipcode;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private BloodType bloodType;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    private String healthPlan;

    private String guardianName;
    private String guardianPhone;
    private String guardianRelationship;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

