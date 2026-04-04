package com.jettech.api.solutions_clinic.model.usecase.professional;

import com.jettech.api.solutions_clinic.model.entity.Professional;
import com.jettech.api.solutions_clinic.model.entity.Role;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.User;
import com.jettech.api.solutions_clinic.model.entity.UserTenantRole;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.model.repository.UserRepository;
import com.jettech.api.solutions_clinic.model.repository.UserTenantRoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.DuplicateEntityException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.security.TenantContext;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultAddProfessionalToClinicUseCase implements AddProfessionalToClinicUseCase {

    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public ProfessionalResponse execute(AddProfessionalToClinicRequest request) throws AuthenticationFailedException {
        // Validar se o usuário existe
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário", request.userId()));

        // Validar se o tenant/clínica existe
        tenantContext.requireSameTenant(request.tenantId());
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Clínica", request.tenantId()));

        // Validar se já existe profissional com mesmo user e tenant
        professionalRepository.findByUserIdAndTenantId(request.userId(), request.tenantId())
                .ifPresent(professional -> {
                    throw new DuplicateEntityException(ApiError.DUPLICATE_PROFESSIONAL);
                });

        // Criar Professional
        Professional professional = new Professional();
        professional.setUser(user);
        professional.setTenant(tenant);
        professional.setSpecialty(request.specialty());
        professional.setDocumentType(request.documentType());
        professional.setDocumentNumber(request.documentNumber());
        professional.setDocumentState(request.documentState());
        professional.setBio(request.bio());
        professional.setActive(true);

        professional = professionalRepository.save(professional);

        // Criar role SPECIALIST automaticamente para o usuário no tenant
        if (!userTenantRoleRepository.existsByUserAndTenantAndRole(user, tenant, Role.SPECIALIST)) {
            UserTenantRole userTenantRole = new UserTenantRole();
            userTenantRole.setUser(user);
            userTenantRole.setTenant(tenant);
            userTenantRole.setRole(Role.SPECIALIST);
            userTenantRoleRepository.save(userTenantRole);
        }

        // Converter para Response
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

