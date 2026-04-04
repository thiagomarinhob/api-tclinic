package com.jettech.api.solutions_clinic.model.usecase.professional;

import com.jettech.api.solutions_clinic.model.entity.Professional;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
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
public class DefaultGetProfessionalsByClinicUseCase implements GetProfessionalsByClinicUseCase {

    private final ProfessionalRepository professionalRepository;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public Page<ProfessionalResponse> execute(GetProfessionalsByClinicRequest request) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(request.clinicId());
        tenantRepository.findById(request.clinicId())
                .orElseThrow(() -> new EntityNotFoundException("Clínica", request.clinicId()));

        // Criar Pageable com ordenação
        Pageable pageable = createPageable(request.page(), request.size(), request.sort());

        // Buscar profissionais com filtros e paginação
        Page<Professional> professionalsPage = professionalRepository.findByTenantIdWithFilters(
                request.clinicId(),
                request.search(),
                request.active(),
                request.documentType(),
                pageable
        );

        // Converter para Page<ProfessionalResponse>
        return professionalsPage.map(this::toResponse);
    }

    private Pageable createPageable(int page, int size, String sort) {
        Sort sortObj;
        
        if (sort != null && !sort.isEmpty()) {
            // Parse do sort string: "field,direction" ou "field"
            String[] sortParts = sort.split(",");
            String field = sortParts[0].trim();
            String direction = sortParts.length > 1 ? sortParts[1].trim() : "ASC";
            
            // Mapear campos do frontend para campos da entidade
            sortObj = mapSortField(field, direction);
        } else {
            // Ordenação padrão: por nome do usuário ascendente
            sortObj = Sort.by(Sort.Direction.ASC, "user.firstName", "user.lastName");
        }
        
        return PageRequest.of(page, size, sortObj);
    }

    private Sort mapSortField(String field, String direction) {
        // Mapear campos do frontend para campos da entidade JPA
        Sort.Direction sortDirection = "DESC".equals(direction.toUpperCase()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        return switch (field) {
            case "user.fullName", "fullName", "name" -> {
                // Ordena por firstName primeiro, depois lastName
                yield Sort.by(sortDirection, "user.firstName", "user.lastName");
            }
            case "specialty" -> Sort.by(sortDirection, "specialty");
            case "documentType" -> Sort.by(sortDirection, "documentType");
            case "documentNumber" -> Sort.by(sortDirection, "documentNumber");
            case "active", "isActive", "status" -> Sort.by(sortDirection, "active");
            case "createdAt" -> Sort.by(sortDirection, "createdAt");
            case "updatedAt" -> Sort.by(sortDirection, "updatedAt");
            default -> Sort.by(sortDirection, "user.firstName", "user.lastName"); // Default
        };
    }

    private ProfessionalResponse toResponse(Professional professional) {
        // Acessar user e tenant dentro da transação para evitar LazyInitializationException
        // Como estamos em @Transactional, o Hibernate fará o fetch automaticamente
        return new ProfessionalResponse(
                professional.getId(),
                professional.getUser().getId(),
                professional.getTenant().getId(),
                professional.getSpecialty(),
                professional.getDocumentType(),
                professional.getDocumentNumber(),
                professional.getDocumentState(),
                professional.getBio(),
                professional.isActive(),
                professional.getCreatedAt(),
                professional.getUpdatedAt()
        );
    }
}
