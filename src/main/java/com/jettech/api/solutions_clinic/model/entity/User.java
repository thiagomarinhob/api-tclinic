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
@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String firstName;
    private String lastName;
    private String phone;

    @Column(unique = true)
    @Pattern(regexp = "\\d{11}", message = "O campo [cpf] deve conter exatamente 11 dígitos")
    private String cpf;

    @Column(nullable = true, length = 10)
    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "O campo [birthDate] deve ser uma data válida no formato DD/MM/YYYY")
    private String birthDate;

    @Column(unique = true, nullable = false)
    @Email( message = "O campo [email] deve ser um email válido" )
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean blocked = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
