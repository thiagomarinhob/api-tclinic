package com.jettech.api.solutions_clinic.model.usecase.professional;

import com.jettech.api.solutions_clinic.model.entity.Professional;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetProfessionalTenantsUseCase implements GetProfessionalTenantsUseCase {

    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ProfessionalTenantResponse execute(UUID userId) throws AuthenticationFailedException {
        // Validar se o usuário existe
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", userId));

        // Buscar todos os profissionais do usuário
        List<Professional> professionals = professionalRepository.findByUserId(userId);

        // Converter para resposta
        List<ProfessionalTenantResponse.TenantInfo> tenantInfos = professionals.stream()
                .map(professional -> new ProfessionalTenantResponse.TenantInfo(
                        professional.getTenant().getId(),
                        professional.getTenant().getName(),
                        professional.getTenant().getCnpj(),
                        professional.getTenant().getSubdomain(),
                        professional.getTenant().getType(),
                        professional.getTenant().getStatus(),
                        professional.getTenant().getPlanType(),
                        professional.getTenant().getTrialEndsAt(),
                        professional.getTenant().isActive(),
                        professional.getId(),
                        professional.getSpecialty(),
                        professional.getCreatedAt(),
                        professional.getUpdatedAt()
                ))
                .collect(Collectors.toList());

        return new ProfessionalTenantResponse(tenantInfos);
    }
}

