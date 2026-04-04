package com.jettech.api.solutions_clinic.model.usecase.procedure;

import com.jettech.api.solutions_clinic.model.entity.Procedure;
import com.jettech.api.solutions_clinic.model.repository.ProcedureRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.security.TenantContext;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetProceduresByTenantUseCase implements GetProceduresByTenantUseCase {

    private final ProcedureRepository procedureRepository;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public Page<ProcedureResponse> execute(GetProceduresByTenantRequest request) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(request.tenantId());
        tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Clínica", request.tenantId()));

        // Criar Pageable com ordenação
        Pageable pageable = createPageable(request.page(), request.size(), request.sort());

        // Buscar procedimentos com filtros (profissional, busca e status) ou listagem simples
        boolean useFilters = request.professionalId() != null
                || (request.search() != null && !request.search().isBlank())
                || request.active() != null;
        Page<Procedure> proceduresPage = useFilters
            ? procedureRepository.findByTenantIdWithFilters(
                request.tenantId(),
                request.professionalId(),
                request.search() != null && !request.search().isBlank() ? request.search().trim() : null,
                request.active(),
                pageable
            )
            : procedureRepository.findByTenantId(request.tenantId(), pageable);

        // Converter para Page<ProcedureResponse>
        return proceduresPage.map(this::toResponse);
    }

    private Pageable createPageable(int page, int size, String sort) {
        Sort sortObj;
        
        if (sort != null && !sort.isEmpty()) {
            // Parse do sort string: "field,direction" ou "field"
            String[] sortParts = sort.split(",");
            String field = sortParts[0].trim();
            String direction = sortParts.length > 1 ? sortParts[1].trim().toUpperCase() : "ASC";
            
            Sort.Direction sortDirection = "DESC".equals(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sortObj = Sort.by(sortDirection, field);
        } else {
            // Ordenação padrão: por name ascendente
            sortObj = Sort.by(Sort.Direction.ASC, "name");
        }
        
        return PageRequest.of(page, size, sortObj);
    }

    private ProcedureResponse toResponse(Procedure procedure) {
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
}
