package com.jettech.api.solutions_clinic.model.usecase.user;

import com.jettech.api.solutions_clinic.model.entity.Role;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.User;
import com.jettech.api.solutions_clinic.model.entity.UserTenantRole;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.model.repository.UserRepository;
import com.jettech.api.solutions_clinic.model.repository.UserTenantRoleRepository;
import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.DuplicateEntityException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateUserUseCase implements CreateUserUseCase {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User execute(CreateUserRequest in) {
        // Validar email duplicado
        userRepository
                .findByEmail(in.email())
                .ifPresent((user) -> {
                    throw new DuplicateEntityException(ApiError.DUPLICATE_EMAIL);
                });

        // Validar CPF duplicado (se informado)
        if (in.cpf() != null && !in.cpf().trim().isEmpty()) {
            userRepository
                    .findByCpf(in.cpf())
                    .ifPresent((user) -> {
                        throw new DuplicateEntityException(ApiError.DUPLICATE_CPF);
                    });
        }

        var password = this.passwordEncoder.encode(in.password());

        final User user = new User();
        user.setFirstName(in.firstName());
        user.setLastName(in.lastName());
        user.setEmail(in.email());
        user.setPassword(password);
        
        // Campos opcionais
        if (in.phone() != null && !in.phone().trim().isEmpty()) {
            user.setPhone(in.phone());
        }
        if (in.cpf() != null && !in.cpf().trim().isEmpty()) {
            user.setCpf(in.cpf());
        }
        if (in.birthDate() != null && !in.birthDate().trim().isEmpty()) {
            user.setBirthDate(in.birthDate());
        }

        userRepository.save(user);

        // Se tenantId foi fornecido, criar role RECEPTION automaticamente
        if (in.tenantId() != null) {
            Tenant tenant = tenantRepository.findById(in.tenantId())
                    .orElseThrow(() -> new EntityNotFoundException("Clínica", in.tenantId()));
            
            // Verificar se a associação já existe antes de criar
            if (!userTenantRoleRepository.existsByUserAndTenantAndRole(user, tenant, Role.RECEPTION)) {
                UserTenantRole userTenantRole = new UserTenantRole();
                userTenantRole.setUser(user);
                userTenantRole.setTenant(tenant);
                userTenantRole.setRole(Role.RECEPTION);
                userTenantRoleRepository.save(userTenantRole);
            }
        }

        return user;
    }

}
