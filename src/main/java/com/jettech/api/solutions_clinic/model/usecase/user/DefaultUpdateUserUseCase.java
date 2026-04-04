package com.jettech.api.solutions_clinic.model.usecase.user;

import com.jettech.api.solutions_clinic.model.entity.User;
import com.jettech.api.solutions_clinic.model.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.DuplicateEntityException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultUpdateUserUseCase implements UpdateUserUseCase {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse execute(UpdateUserRequest request) throws AuthenticationFailedException {
        User user = userRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Usuário", request.id()));

        // Atualizar campos se fornecidos
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.cpf() != null && !request.cpf().trim().isEmpty()) {
            // Validar se o CPF já está em uso por outro usuário
            final User finalUser = user;
            userRepository.findByCpf(request.cpf())
                    .ifPresent((existingUser) -> {
                        if (!existingUser.getId().equals(finalUser.getId())) {
                            throw new DuplicateEntityException(ApiError.DUPLICATE_CPF);
                        }
                    });
            user.setCpf(request.cpf());
        } else if (request.cpf() != null && request.cpf().trim().isEmpty()) {
            // Se CPF for string vazia, remover o CPF
            user.setCpf(null);
        }
        if (request.birthDate() != null) {
            user.setBirthDate(request.birthDate());
        }
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            // Verificar se o email já existe
            final User finalUser = user;
            userRepository.findByEmail(request.email())
                    .ifPresent((existingUser) -> {
                        if (!existingUser.getId().equals(finalUser.getId())) {
                            throw new DuplicateEntityException(ApiError.DUPLICATE_EMAIL);
                        }
                    });
            user.setEmail(request.email());
        }

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getCpf(),
                savedUser.getBirthDate(),
                savedUser.isBlocked(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt()
        );
    }

    public boolean checkCpfExists(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }
        return userRepository.findByCpf(cpf).isPresent();
    }
}
