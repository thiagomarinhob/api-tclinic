package com.jettech.api.solutions_clinic.model.usecase.user;

import com.jettech.api.solutions_clinic.model.entity.User;
import com.jettech.api.solutions_clinic.model.entity.UserTenantRole;
import com.jettech.api.solutions_clinic.model.repository.UserRepository;
import com.jettech.api.solutions_clinic.model.repository.UserTenantRoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetUserByIdUseCase implements GetUserByIdUseCase {

    private final UserRepository userRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse execute(UUID userId) throws AuthenticationFailedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", userId));

        List<UserTenantRole> userTenantRoles = userTenantRoleRepository.findByUser_Id(userId);
        UUID contextClinicId = tenantContext.getRequiredClinicId();
        boolean hasAccessToClinic = userTenantRoles.stream()
                .anyMatch(utr -> utr.getTenant().getId().equals(contextClinicId));
        if (!hasAccessToClinic) {
            throw new ForbiddenException();
        }

        List<UserDetailResponse.TenantRoleInfo> tenantRoles = userTenantRoles.stream()
                .map(utr -> new UserDetailResponse.TenantRoleInfo(
                        utr.getTenant().getId(),
                        utr.getTenant().getName(),
                        utr.getTenant().getSubdomain(),
                        utr.getTenant().getType(),
                        utr.getTenant().getStatus(),
                        utr.getTenant().getPlanType(),
                        utr.getTenant().getTrialEndsAt(),
                        utr.getTenant().isActive(),
                        utr.getRole()
                ))
                .collect(Collectors.toList());

        return new UserDetailResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                tenantRoles
        );
    }
}

