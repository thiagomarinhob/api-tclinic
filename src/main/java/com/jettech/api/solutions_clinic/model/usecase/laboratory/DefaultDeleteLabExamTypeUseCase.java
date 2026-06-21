package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.LabExamType;
import com.jettech.api.solutions_clinic.model.repository.LabExamTypeRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultDeleteLabExamTypeUseCase implements DeleteLabExamTypeUseCase {

    private final LabExamTypeRepository labExamTypeRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public void execute(UUID id) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        log.info("Deletando tipo de exame lab - examTypeId: {}, tenantId: {}", id, tenantId);
        LabExamType examType = labExamTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de exame", id));
        if (!examType.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado ao deletar tipo de exame {} - tenantId: {}", id, tenantId);
            throw new ForbiddenException();
        }
        labExamTypeRepository.delete(examType);
        log.info("Tipo de exame lab deletado - examTypeId: {}", id);
    }
}
