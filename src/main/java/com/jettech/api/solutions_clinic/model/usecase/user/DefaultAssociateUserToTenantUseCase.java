package com.jettech.api.solutions_clinic.model.usecase.user;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.User;
import com.jettech.api.solutions_clinic.model.entity.UserTenantRole;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.model.repository.UserRepository;
import com.jettech.api.solutions_clinic.model.repository.UserTenantRoleRepository;
import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.DuplicateEntityException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultAssociateUserToTenantUseCase implements AssociateUserToTenantUseCase {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public Void execute(AssociateUserToTenantRequest request) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(request.tenantId());
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário", request.userId()));

        // Validar se o tenant existe
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Clínica", request.tenantId()));

        // Validar se a associação já existe
        if (userTenantRoleRepository.existsByUserAndTenantAndRole(user, tenant, request.role())) {
            throw new DuplicateEntityException(ApiError.DUPLICATE_USER_TENANT_ROLE);
        }

        // Criar a associação
        UserTenantRole userTenantRole = new UserTenantRole();
        userTenantRole.setUser(user);
        userTenantRole.setTenant(tenant);
        userTenantRole.setRole(request.role());

        userTenantRoleRepository.save(userTenantRole);

        return null;
    }
}
