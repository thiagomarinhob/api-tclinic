package com.jettech.api.solutions_clinic.model.usecase.signup;

import com.jettech.api.solutions_clinic.model.entity.Role;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.entity.TypeTenant;
import com.jettech.api.solutions_clinic.model.entity.User;
import com.jettech.api.solutions_clinic.model.entity.UserTenantRole;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.model.repository.UserRepository;
import com.jettech.api.solutions_clinic.model.repository.UserTenantRoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.DuplicateEntityException;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultSignUpSoloUseCase implements SignUpSoloUseCase {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public SignUpResponse execute(SignUpSoloRequest request) throws AuthenticationFailedException {
        // Validações
        validateEmailNotExists(request.email());
        validateSubdomainNotExists(request.subdomain());

        // Criar Tenant (para SOLO, não precisa de CNPJ)
        Tenant tenant = createTenant(request, TypeTenant.SOLO);
        tenant = tenantRepository.save(tenant);

        // Criar User
        User user = createUser(request);
        user = userRepository.save(user);

        // Vincular User e Tenant com Role OWNER
        UserTenantRole userTenantRole = createUserTenantRole(user, tenant, Role.OWNER);
        userTenantRoleRepository.save(userTenantRole);

        return new SignUpResponse(
            user.getId(),
            tenant.getId(),
            user.getEmail(),
            tenant.getName(),
            tenant.getSubdomain()
        );
    }

    private void validateEmailNotExists(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new DuplicateEntityException(ApiError.DUPLICATE_EMAIL);
        });
    }

    private void validateSubdomainNotExists(String subdomain) {
        tenantRepository.findBySubdomain(subdomain).ifPresent(tenant -> {
            throw new DuplicateEntityException(ApiError.DUPLICATE_SUBDOMAIN);
        });
    }

    private Tenant createTenant(SignUpSoloRequest request, TypeTenant type) {
        Tenant tenant = new Tenant();
        tenant.setName(request.name());
        // Para SOLO, não usamos CNPJ, mas podemos armazenar o CPF no campo cnpj se necessário
        // ou criar um campo separado. Por enquanto, deixamos null
        tenant.setCnpj(null);
        tenant.setPlanType(request.planType());
        tenant.setAddress(request.address());
        tenant.setPhone(request.phone());
        tenant.setSubdomain(request.subdomain().toLowerCase());
        tenant.setType(type);
        tenant.setStatus(TenantStatus.PENDING_SETUP);
        tenant.setActive(true);
        return tenant;
    }

    private User createUser(SignUpSoloRequest request) {
        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setBirthDate(request.birthDate());
        user.setCpf(request.cpf());
        return user;
    }

    private UserTenantRole createUserTenantRole(User user, Tenant tenant, Role role) {
        UserTenantRole userTenantRole = new UserTenantRole();
        userTenantRole.setUser(user);
        userTenantRole.setTenant(tenant);
        userTenantRole.setRole(role);
        return userTenantRole;
    }
}

