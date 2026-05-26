package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.ProcedureComboItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcedureComboItemRepository extends JpaRepository<ProcedureComboItem, UUID> {
}
