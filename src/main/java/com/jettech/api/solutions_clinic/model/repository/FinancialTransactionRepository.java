package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.FinancialTransaction;
import com.jettech.api.solutions_clinic.model.entity.PaymentStatus;
import com.jettech.api.solutions_clinic.model.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {

    List<FinancialTransaction> findByTenantId(UUID tenantId);
    
    List<FinancialTransaction> findByTenantIdAndType(UUID tenantId, TransactionType type);
    
    List<FinancialTransaction> findByTenantIdAndStatus(UUID tenantId, PaymentStatus status);
    
    List<FinancialTransaction> findByTenantIdAndTypeAndStatus(UUID tenantId, TransactionType type, PaymentStatus status);
    
    List<FinancialTransaction> findByAppointmentId(UUID appointmentId);
    
    List<FinancialTransaction> findByProfessionalId(UUID professionalId);
    
    // 1. Saldo total por período (Receitas - Despesas deve ser calculado no service ou numa query mais complexa)
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM financial_transactions f " +
           "WHERE f.tenant.id = :tenantId " +
           "AND f.type = :type " +
           "AND f.status = 'PAGO' " +
           "AND f.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumByTypeAndDateRange(@Param("tenantId") UUID tenantId, 
                                     @Param("type") TransactionType type,
                                     @Param("startDate") LocalDate startDate, 
                                     @Param("endDate") LocalDate endDate);

    // 2. Movimentações por Profissional (para ver quanto cada um gerou)
    @Query("SELECT f FROM financial_transactions f " +
           "WHERE f.tenant.id = :tenantId " +
           "AND f.appointment.professional.id = :professionalId " +
           "AND f.paymentDate BETWEEN :startDate AND :endDate")
    List<FinancialTransaction> findByProfessionalAndDate(@Param("tenantId") UUID tenantId,
                                                         @Param("professionalId") UUID professionalId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    // 3. Agrupamento por Categoria (Gráfico de Pizza de despesas)
    @Query("SELECT f.category.name, SUM(f.amount) FROM financial_transactions f " +
           "WHERE f.tenant.id = :tenantId " +
           "AND f.type = 'EXPENSE' " +
           "AND f.status = 'PAGO' " +
           "AND f.paymentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY f.category.name")
    List<Object[]> sumExpensesByCategory(@Param("tenantId") UUID tenantId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
    
    // 4. Agrupamento por Categoria de Receitas
    @Query("SELECT f.category.name, SUM(f.amount) FROM financial_transactions f " +
           "WHERE f.tenant.id = :tenantId " +
           "AND f.type = 'INCOME' " +
           "AND f.status = 'PAGO' " +
           "AND f.paymentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY f.category.name")
    List<Object[]> sumIncomesByCategory(@Param("tenantId") UUID tenantId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
    
    // 5. Transações por período
    @Query("SELECT f FROM financial_transactions f " +
           "WHERE f.tenant.id = :tenantId " +
           "AND f.paymentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY f.paymentDate DESC")
    List<FinancialTransaction> findByTenantIdAndPaymentDateBetween(@Param("tenantId") UUID tenantId,
                                                                    @Param("startDate") LocalDate startDate,
                                                                    @Param("endDate") LocalDate endDate);
    
    // 6. Transações pendentes (Contas a Pagar/Receber)
    @Query("SELECT f FROM financial_transactions f " +
           "WHERE f.tenant.id = :tenantId " +
           "AND f.status = 'PENDENTE' " +
           "AND f.dueDate BETWEEN :startDate AND :endDate " +
           "ORDER BY f.dueDate ASC")
    List<FinancialTransaction> findPendingByTenantIdAndDueDateBetween(@Param("tenantId") UUID tenantId,
                                                                       @Param("startDate") LocalDate startDate,
                                                                       @Param("endDate") LocalDate endDate);
}
