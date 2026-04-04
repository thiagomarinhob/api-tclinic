package com.jettech.api.solutions_clinic.model.usecase.financial;

import com.jettech.api.solutions_clinic.model.entity.*;
import com.jettech.api.solutions_clinic.model.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.InvalidRequestException;
import com.jettech.api.solutions_clinic.security.TenantContext;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateFinancialTransactionUseCase implements CreateFinancialTransactionUseCase {

    private final FinancialTransactionRepository financialTransactionRepository;
    private final FinancialCategoryRepository financialCategoryRepository;
    private final TenantRepository tenantRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProfessionalRepository professionalRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public FinancialTransactionResponse execute(CreateFinancialTransactionRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", tenantId));

        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setTenant(tenant);
        transaction.setDescription(request.description());
        transaction.setType(request.type());
        transaction.setAmount(request.amount());
        transaction.setDueDate(request.dueDate());
        transaction.setPaymentDate(request.paymentDate());
        transaction.setStatus(request.status());
        transaction.setPaymentMethod(request.paymentMethod());

        // Associar categoria se fornecida (deve ser do mesmo tenant)
        if (request.categoryId() != null) {
            FinancialCategory category = financialCategoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria", request.categoryId()));
            if (!category.getTenant().getId().equals(tenantId)) {
                throw new com.jettech.api.solutions_clinic.exception.ForbiddenException();
            }
            if (category.getType() != request.type()) {
                throw new InvalidRequestException(ApiError.CATEGORY_TYPE_MISMATCH);
            }
            transaction.setCategory(category);
        }

        if (request.appointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.appointmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Agendamento", request.appointmentId()));
            if (!appointment.getTenant().getId().equals(tenantId)) {
                throw new com.jettech.api.solutions_clinic.exception.ForbiddenException();
            }
            transaction.setAppointment(appointment);
        }

        if (request.professionalId() != null) {
            Professional professional = professionalRepository.findById(request.professionalId())
                    .orElseThrow(() -> new EntityNotFoundException("Profissional", request.professionalId()));
            if (!professional.getTenant().getId().equals(tenantId)) {
                throw new com.jettech.api.solutions_clinic.exception.ForbiddenException();
            }
            transaction.setProfessional(professional);
        }

        transaction = financialTransactionRepository.save(transaction);

        return toResponse(transaction);
    }

    private FinancialTransactionResponse toResponse(FinancialTransaction transaction) {
        return new FinancialTransactionResponse(
                transaction.getId(),
                transaction.getTenant().getId(),
                transaction.getDescription(),
                transaction.getType(),
                transaction.getCategory() != null ? transaction.getCategory().getId() : null,
                transaction.getCategory() != null ? transaction.getCategory().getName() : null,
                transaction.getAmount(),
                transaction.getDueDate(),
                transaction.getPaymentDate(),
                transaction.getStatus(),
                transaction.getPaymentMethod(),
                transaction.getAppointment() != null ? transaction.getAppointment().getId() : null,
                transaction.getProfessional() != null ? transaction.getProfessional().getId() : null,
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
