package com.jettech.api.solutions_clinic.model.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Entity(name = "procedure_combo_items")
public class ProcedureComboItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_procedure_id", nullable = false)
    private Procedure comboProcedure;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "item_procedure_id", nullable = false)
    private Procedure itemProcedure;
}
