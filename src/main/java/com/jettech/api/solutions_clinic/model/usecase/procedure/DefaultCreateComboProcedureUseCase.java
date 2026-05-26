package com.jettech.api.solutions_clinic.model.usecase.procedure;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.entity.Procedure;
import com.jettech.api.solutions_clinic.model.entity.ProcedureComboItem;
import com.jettech.api.solutions_clinic.model.entity.Professional;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.ProcedureRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateComboProcedureUseCase implements CreateComboProcedureUseCase {

    private final ProcedureRepository procedureRepository;
    private final TenantRepository tenantRepository;
    private final ProfessionalRepository professionalRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public ProcedureResponse execute(CreateComboProcedureRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", tenantId));

        Professional professional = resolveProfessional(request.professionalId(), tenantId);

        // Load and validate item procedures
        List<Procedure> itemProcedures = request.itemProcedureIds().stream()
                .map(id -> procedureRepository.findByIdAndTenantId(id, tenantId)
                        .orElseThrow(() -> new EntityNotFoundException("Procedimento", id)))
                .toList();

        // Calculate total duration from items
        int totalDuration = itemProcedures.stream()
                .mapToInt(Procedure::getEstimatedDurationMinutes)
                .sum();

        // Create the combo procedure
        Procedure combo = new Procedure();
        combo.setTenant(tenant);
        combo.setProfessional(professional);
        combo.setName(request.name());
        combo.setDescription(request.description());
        combo.setEstimatedDurationMinutes(Math.max(totalDuration, 1));
        combo.setBasePrice(request.basePrice());
        combo.setActive(true);
        combo.setCombo(true);

        combo = procedureRepository.save(combo);

        // Create combo item links
        for (Procedure item : itemProcedures) {
            ProcedureComboItem link = new ProcedureComboItem();
            link.setComboProcedure(combo);
            link.setItemProcedure(item);
            combo.getComboItems().add(link);
        }

        combo = procedureRepository.save(combo);

        return ProcedureResponse.from(combo);
    }

    private Professional resolveProfessional(UUID requestedProfessionalId, UUID tenantId) throws AuthenticationFailedException {
        if (requestedProfessionalId != null) {
            return professionalRepository.findByIdAndTenantId(requestedProfessionalId, tenantId)
                    .orElseThrow(() -> new EntityNotFoundException("Profissional", requestedProfessionalId));
        }
        UUID userId = getUserIdFromContext();
        if (userId == null) {
            throw new AuthenticationFailedException("Usuário não identificado e profissional não informado");
        }
        return professionalRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Profissional não encontrado para o usuário atual nesta clínica."));
    }

    private UUID getUserIdFromContext() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                String subject = jwt.getSubject();
                if (subject != null) return UUID.fromString(subject);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
