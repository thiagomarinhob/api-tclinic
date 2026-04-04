package com.jettech.api.solutions_clinic.model.usecase.professional;

import com.jettech.api.solutions_clinic.model.entity.Professional;
import com.jettech.api.solutions_clinic.model.entity.Role;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.User;
import com.jettech.api.solutions_clinic.model.entity.UserTenantRole;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.model.repository.UserTenantRoleRepository;
import com.jettech.api.solutions_clinic.model.usecase.user.CreateUserRequest;
import com.jettech.api.solutions_clinic.model.usecase.user.CreateUserUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.DuplicateEntityException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateProfessionalWithUserUseCase implements CreateProfessionalWithUserUseCase {

    private final ProfessionalRepository professionalRepository;
    private final TenantRepository tenantRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;
    private final CreateUserUseCase createUserUseCase;
    private final com.jettech.api.solutions_clinic.security.TenantContext tenantContext;

    @Override
    @Transactional
    public ProfessionalResponse execute(CreateProfessionalWithUserRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", tenantId));

        // Criar o usuário primeiro
        CreateUserRequest createUserRequest = new CreateUserRequest(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password(),
                request.phone(),
                request.cpf() != null && !request.cpf().trim().isEmpty() ? request.cpf() : null,
                request.birthDate() != null && !request.birthDate().trim().isEmpty() ? request.birthDate() : null,
                tenantId // Passar tenantId para criar a role RECEPTION automaticamente (será substituída por SPECIALIST)
        );
        
        User user = createUserUseCase.execute(createUserRequest);

        // Validar se já existe profissional com mesmo user e tenant
        professionalRepository.findByUserIdAndTenantId(user.getId(), tenantId)
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

        // Remover role RECEPTION e criar role SPECIALIST para o usuário no tenant
        userTenantRoleRepository.findByUserAndTenant(user, tenant)
                .forEach(userTenantRole -> {
                    if (userTenantRole.getRole() == Role.RECEPTION) {
                        userTenantRoleRepository.delete(userTenantRole);
                    }
                });

        // Criar role SPECIALIST
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
