package com.jettech.api.solutions_clinic.model.usecase.procedure;

import com.jettech.api.solutions_clinic.model.entity.Professional;
import com.jettech.api.solutions_clinic.model.entity.Procedure;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.ProcedureRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateProcedureUseCase implements CreateProcedureUseCase {

    private final ProcedureRepository procedureRepository;
    private final TenantRepository tenantRepository;
    private final ProfessionalRepository professionalRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public ProcedureResponse execute(CreateProcedureRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", tenantId));
                
        // Obter usuário logado
        UUID userId = getUserIdFromContext();
        if (userId == null && request.professionalId() == null) {
            throw new AuthenticationFailedException("Usuário não identificado e profissional não informado");
        }
        
        Professional professional;
        if (request.professionalId() != null) {
            professional = professionalRepository.findByIdAndTenantId(request.professionalId(), tenantId)
                    .orElseThrow(() -> new EntityNotFoundException("Profissional", request.professionalId()));
        } else {
            professional = professionalRepository.findByUserIdAndTenantId(userId, tenantId)
                    .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado para o usuário atual nesta clínica. Apenas profissionais podem criar procedimentos ou é necessário informar o ID do profissional."));
        }

        Procedure procedure = new Procedure();
        procedure.setTenant(tenant);
        procedure.setProfessional(professional);
        procedure.setName(request.name());
        procedure.setDescription(request.description());
        procedure.setEstimatedDurationMinutes(request.estimatedDurationMinutes());
        procedure.setBasePrice(request.basePrice());
        procedure.setProfessionalCommissionPercent(request.professionalCommissionPercent());
        procedure.setActive(true);

        procedure = procedureRepository.save(procedure);

        return new ProcedureResponse(
                procedure.getId(),
                procedure.getTenant().getId(),
                procedure.getName(),
                procedure.getDescription(),
                procedure.getEstimatedDurationMinutes(),
                procedure.getBasePrice(),
                procedure.getProfessionalCommissionPercent(),
                procedure.isActive(),
                procedure.getCreatedAt(),
                procedure.getUpdatedAt()
        );
    }
    
    private UUID getUserIdFromContext() {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt) {
                org.springframework.security.oauth2.jwt.Jwt jwt = 
                    (org.springframework.security.oauth2.jwt.Jwt) authentication.getPrincipal(); // sub é o userId
                String subject = jwt.getSubject();
                if (subject != null) {
                    return UUID.fromString(subject);
                }
            }
        } catch (Exception e) {
            // Se não conseguir obter, retorna null
        }
        return null;
    }
}
