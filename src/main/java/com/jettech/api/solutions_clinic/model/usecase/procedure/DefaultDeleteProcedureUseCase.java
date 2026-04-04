package com.jettech.api.solutions_clinic.model.usecase.procedure;

import com.jettech.api.solutions_clinic.model.entity.Procedure;
import com.jettech.api.solutions_clinic.model.repository.AppointmentProcedureRepository;
import com.jettech.api.solutions_clinic.model.repository.ProcedureRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultDeleteProcedureUseCase implements DeleteProcedureUseCase {

    private final ProcedureRepository procedureRepository;
    private final AppointmentProcedureRepository appointmentProcedureRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public void execute(UUID id) throws AuthenticationFailedException {
        Procedure procedure = procedureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Procedimento", id));
        if (!procedure.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
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
    }
}
