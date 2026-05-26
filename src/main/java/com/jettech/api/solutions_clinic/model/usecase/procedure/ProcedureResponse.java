package com.jettech.api.solutions_clinic.model.usecase.procedure;

import com.jettech.api.solutions_clinic.model.entity.Procedure;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record ProcedureResponse(
    UUID id,
    UUID tenantId,
    String name,
    String description,
    int estimatedDurationMinutes,
    BigDecimal basePrice,
    BigDecimal professionalCommissionPercent,
    boolean active,
    boolean isCombo,
    List<ProcedureComboItemResponse> comboItems,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ProcedureResponse from(Procedure procedure) {
        List<ProcedureComboItemResponse> items = procedure.isCombo()
                ? procedure.getComboItems().stream()
                        .map(item -> new ProcedureComboItemResponse(
                                item.getId(),
                                item.getItemProcedure().getId(),
                                item.getItemProcedure().getName(),
                                item.getItemProcedure().getEstimatedDurationMinutes(),
                                item.getItemProcedure().getBasePrice()
                        ))
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return new ProcedureResponse(
                procedure.getId(),
                procedure.getTenant().getId(),
                procedure.getName(),
                procedure.getDescription(),
                procedure.getEstimatedDurationMinutes(),
                procedure.getBasePrice(),
                procedure.getProfessionalCommissionPercent(),
                procedure.isActive(),
                procedure.isCombo(),
                items,
                procedure.getCreatedAt(),
                procedure.getUpdatedAt()
        );
    }
}
