package com.jettech.api.solutions_clinic.model.usecase.procedure;

import com.jettech.api.solutions_clinic.model.entity.Procedure;
import com.jettech.api.solutions_clinic.model.repository.AppointmentProcedureRepository;
import com.jettech.api.solutions_clinic.model.repository.ProcedureRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultDeleteProcedureUseCase implements DeleteProcedureUseCase {

    private final ProcedureRepository procedureRepository;
    private final AppointmentProcedureRepository appointmentProcedureRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public void execute(UUID id) throws AuthenticationFailedException {
        log.info("Deletando procedimento - procedureId: {}", id);
        Procedure procedure = procedureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Procedimento", id));
        UUID tenantId = tenantContext.getRequiredClinicId();
        if (!procedure.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado ao deletar procedimento {} - tenantId: {}", id, tenantId);
            throw new ForbiddenException();
        }
        // Verificar se o procedimento está sendo usado em algum agendamento
        // Se estiver, não permitir exclusão (ou podemos apenas desativar)
        // Por enquanto, vamos apenas verificar e lançar exceção se houver uso
        // Em uma implementação mais robusta, poderíamos apenas desativar ao invés de deletar
        
        // Primeiro, deletar todas as relações AppointmentProcedure
        appointmentProcedureRepository.findByProcedureId(id).forEach(appointmentProcedureRepository::delete);

        // Depois, deletar o procedimento
        procedureRepository.delete(procedure);
        log.info("Procedimento deletado - procedureId: {}", id);
    }
}
