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
import java.util.Arrays;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultSignUpClinicOwnerUseCase implements SignUpClinicOwnerUseCase {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public SignUpResponse execute(SignUpClinicOwnerRequest request) throws AuthenticationFailedException {
        // Validações
        validateEmailNotExists(request.email());
        validateCnpjNotExists(request.cnpj());
        validateSubdomainNotExists(request.subdomain());

        // Criar Tenant
        Tenant tenant = createTenant(request, TypeTenant.CLINIC);
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

    private void validateCnpjNotExists(String cnpj) {
        tenantRepository.findByCnpj(cnpj).ifPresent(tenant -> {
            throw new DuplicateEntityException(ApiError.DUPLICATE_CNPJ);
        });
    }

    private void validateSubdomainNotExists(String subdomain) {
        tenantRepository.findBySubdomain(subdomain).ifPresent(tenant -> {
            throw new DuplicateEntityException(ApiError.DUPLICATE_SUBDOMAIN);
        });
    }

    private Tenant createTenant(SignUpClinicOwnerRequest request, TypeTenant type) {
        Tenant tenant = new Tenant();
        tenant.setName(request.name());
        tenant.setCnpj(request.cnpj());
        tenant.setPlanType(request.planType());
        tenant.setAddress(request.address());
        tenant.setPhone(request.phone());
        tenant.setSubdomain(request.subdomain().toLowerCase());
        tenant.setType(type);
        tenant.setStatus(TenantStatus.PENDING_SETUP);
        tenant.setActive(true);
        return tenant;
    }

    private User createUser(SignUpClinicOwnerRequest request) {
        User user = new User();
        
        // Usar o nome da clínica como nome do usuário
        // Se firstName ou lastName não forem fornecidos ou estiverem vazios, usar o nome da clínica
        String firstName = request.firstName();
        String lastName = request.lastName();
        
        if (firstName == null || firstName.trim().isEmpty() || 
            lastName == null || lastName.trim().isEmpty()) {
            // Dividir o nome da clínica: primeira palavra como firstName, restante como lastName
            String[] nameParts = request.name().trim().split("\\s+");
            firstName = nameParts[0];
            lastName = nameParts.length > 1 
                ? String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length))
                : request.name(); // Se tiver apenas uma palavra, usar o nome completo como lastName
        }
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        // Para clínica, birthDate não é obrigatório
        // Para clínica, o CPF pode não ser obrigatório, mas se necessário pode ser adicionado ao request
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

