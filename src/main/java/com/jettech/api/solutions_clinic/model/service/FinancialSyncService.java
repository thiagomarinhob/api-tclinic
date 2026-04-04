package com.jettech.api.solutions_clinic.model.service;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.entity.*;
import com.jettech.api.solutions_clinic.model.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialSyncService {

    private final FinancialTransactionRepository financialTransactionRepository;
    private final FinancialCategoryRepository financialCategoryRepository;
    private final TenantRepository tenantRepository;
    private final AppointmentProcedureRepository appointmentProcedureRepository;

    /**
     * Sincroniza uma transação financeira quando um Appointment é pago ou finalizado.
     * Cria uma receita (INCOME) e, se houver comissão configurada, cria uma despesa (EXPENSE) para o profissional.
     */
    @Transactional
    public void syncAppointmentPayment(Appointment appointment) {
        // Verificar se já existe uma transação para este appointment
        List<FinancialTransaction> existingTransactions = financialTransactionRepository.findByAppointmentId(appointment.getId());
        
        // Se já existe transação de receita, não criar novamente
        boolean hasIncomeTransaction = existingTransactions.stream()
                .anyMatch(t -> t.getType() == TransactionType.INCOME);
        
        if (!hasIncomeTransaction && appointment.getPaymentStatus() == PaymentStatus.PAGO) {
            // Criar receita (INCOME) da consulta
            createIncomeTransaction(appointment);
            
            // Criar despesa (EXPENSE) de comissão se houver procedimentos com comissão
            createCommissionExpense(appointment);
        } else if (hasIncomeTransaction && appointment.getPaymentStatus() != PaymentStatus.PAGO) {
            // Se o pagamento foi cancelado ou alterado, cancelar a transação
            cancelIncomeTransaction(appointment.getId());
        }
    }

    private void createIncomeTransaction(Appointment appointment) {
        // Buscar ou criar categoria padrão "Consultas"
        FinancialCategory category = getOrCreateDefaultCategory(
                appointment.getTenant().getId(),
                "Consultas",
                TransactionType.INCOME
        );

        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setTenant(appointment.getTenant());
        transaction.setDescription("Consulta - " + appointment.getPatient().getFirstName());
        transaction.setType(TransactionType.INCOME);
        transaction.setCategory(category);
        transaction.setAmount(appointment.getTotalValue());
        transaction.setDueDate(appointment.getScheduledAt().toLocalDate());
        transaction.setPaymentDate(appointment.getPaidAt() != null ? 
                appointment.getPaidAt().toLocalDate() : LocalDate.now());
        transaction.setStatus(appointment.getPaymentStatus());
        transaction.setPaymentMethod(appointment.getPaymentMethod());
        transaction.setAppointment(appointment);

        financialTransactionRepository.save(transaction);
    }

    private void createCommissionExpense(Appointment appointment) {
        // Calcular comissão total dos procedimentos
        // Carregar procedimentos do appointment (pode estar lazy)
        List<AppointmentProcedure> appointmentProcedures = appointmentProcedureRepository.findByAppointmentId(appointment.getId());
        
        BigDecimal totalCommission = BigDecimal.ZERO;
        
        if (appointmentProcedures != null && !appointmentProcedures.isEmpty()) {
            for (AppointmentProcedure appointmentProcedure : appointmentProcedures) {
                Procedure procedure = appointmentProcedure.getProcedure();
                if (procedure != null && procedure.getProfessionalCommissionPercent() != null) {
                    BigDecimal commission = appointmentProcedure.getFinalPrice()
                            .multiply(procedure.getProfessionalCommissionPercent())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    totalCommission = totalCommission.add(commission);
                }
            }
        }

        // Se houver comissão, criar despesa
        if (totalCommission.compareTo(BigDecimal.ZERO) > 0) {
            // Buscar ou criar categoria padrão "Comissões"
            FinancialCategory category = getOrCreateDefaultCategory(
                    appointment.getTenant().getId(),
                    "Comissões",
                    TransactionType.EXPENSE
            );

            FinancialTransaction commissionTransaction = new FinancialTransaction();
            commissionTransaction.setTenant(appointment.getTenant());
            commissionTransaction.setDescription("Comissão - " + appointment.getProfessional().getUser().getFirstName());
            commissionTransaction.setType(TransactionType.EXPENSE);
            commissionTransaction.setCategory(category);
            commissionTransaction.setAmount(totalCommission);
            commissionTransaction.setDueDate(appointment.getScheduledAt().toLocalDate().plusDays(30)); // Vencimento em 30 dias
            commissionTransaction.setStatus(PaymentStatus.PENDENTE); // Comissão fica pendente até ser paga
            commissionTransaction.setAppointment(appointment);
            commissionTransaction.setProfessional(appointment.getProfessional());

            financialTransactionRepository.save(commissionTransaction);
        }
    }

    private void cancelIncomeTransaction(UUID appointmentId) {
        List<FinancialTransaction> transactions = financialTransactionRepository.findByAppointmentId(appointmentId);
        for (FinancialTransaction transaction : transactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                transaction.setStatus(PaymentStatus.CANCELADO);
                financialTransactionRepository.save(transaction);
            }
        }
    }

    private FinancialCategory getOrCreateDefaultCategory(UUID tenantId, String categoryName, TransactionType type) {
        return financialCategoryRepository.findByNameAndTenantId(categoryName, tenantId)
                .orElseGet(() -> {
                    Tenant tenant = tenantRepository.findById(tenantId)
                            .orElseThrow(() -> new EntityNotFoundException("Clínica", tenantId));
                    
                    FinancialCategory category = new FinancialCategory();
                    category.setTenant(tenant);
                    category.setName(categoryName);
                    category.setType(type);
                    category.setActive(true);
                    return financialCategoryRepository.save(category);
                });
    }
}
