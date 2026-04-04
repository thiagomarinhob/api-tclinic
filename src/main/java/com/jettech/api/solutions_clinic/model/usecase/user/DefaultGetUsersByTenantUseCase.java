package com.jettech.api.solutions_clinic.model.usecase.user;

import com.jettech.api.solutions_clinic.model.entity.User;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.model.repository.UserRepository;
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
public class DefaultGetUsersByTenantUseCase implements GetUsersByTenantUseCase {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> execute(GetUsersByTenantRequest request) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(request.tenantId());
        tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Clínica", request.tenantId()));

        // Criar Pageable com ordenação
        Pageable pageable = createPageable(request.page(), request.size(), request.sort());

        // Buscar usuários com filtros e paginação
        Page<User> usersPage = userRepository.findUsersByTenantIdWithFilters(
                request.tenantId(),
                request.search(),
                request.blocked(),
                request.role(),
                pageable
        );

        // Converter para Page<UserResponse>
        return usersPage.map(this::toResponse);
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
            // Ordenação padrão: por firstName ascendente
            sortObj = Sort.by(Sort.Direction.ASC, "firstName", "lastName");
        }
        
        return PageRequest.of(page, size, sortObj);
    }

    private Sort mapSortField(String field, String direction) {
        // Mapear campos do frontend para campos da entidade JPA
        Sort.Direction sortDirection = "DESC".equals(direction.toUpperCase()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        return switch (field) {
            case "fullName", "name" -> Sort.by(sortDirection, "firstName", "lastName");
            case "email" -> Sort.by(sortDirection, "email");
            case "phone" -> Sort.by(sortDirection, "phone");
            case "cpf" -> Sort.by(sortDirection, "cpf");
            case "blocked", "isBlocked" -> Sort.by(sortDirection, "blocked");
            case "createdAt" -> Sort.by(sortDirection, "createdAt");
            case "updatedAt" -> Sort.by(sortDirection, "updatedAt");
            default -> Sort.by(sortDirection, "firstName", "lastName"); // Default
        };
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getCpf(),
                user.getBirthDate(),
                user.isBlocked(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
