package com.jettech.api.solutions_clinic.model.usecase.user;

import com.jettech.api.solutions_clinic.model.entity.User;
import com.jettech.api.solutions_clinic.model.repository.UserRepository;
import com.jettech.api.solutions_clinic.model.repository.UserTenantRoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultDeleteUserUseCase implements DeleteUserUseCase {

    private final UserRepository userRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;

    @Override
    @Transactional
    public void execute(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", userId));

        // Primeiro, deletar todas as relações UserTenantRole
        userTenantRoleRepository.findByUser_Id(userId).forEach(userTenantRoleRepository::delete);

        // Depois, deletar o usuário
        userRepository.delete(user);
    }
}
