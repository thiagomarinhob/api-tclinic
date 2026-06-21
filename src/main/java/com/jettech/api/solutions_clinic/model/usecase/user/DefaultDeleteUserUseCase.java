package com.jettech.api.solutions_clinic.model.usecase.user;

import com.jettech.api.solutions_clinic.model.entity.User;
import com.jettech.api.solutions_clinic.model.repository.UserRepository;
import com.jettech.api.solutions_clinic.model.repository.UserTenantRoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultDeleteUserUseCase implements DeleteUserUseCase {

    private final UserRepository userRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;

    @Override
    @Transactional
    public void execute(UUID userId) {
        log.info("Deletando usuário - userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", userId));

        // Primeiro, deletar todas as relações UserTenantRole
        var roles = userTenantRoleRepository.findByUser_Id(userId);
        log.debug("Removendo {} relações UserTenantRole para userId: {}", roles.size(), userId);
        roles.forEach(userTenantRoleRepository::delete);

        // Depois, deletar o usuário
        userRepository.delete(user);
        log.info("Usuário deletado - userId: {}, email: {}", userId, user.getEmail());
    }
}
