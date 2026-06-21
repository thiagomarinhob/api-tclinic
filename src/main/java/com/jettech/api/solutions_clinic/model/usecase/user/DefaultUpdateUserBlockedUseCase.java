package com.jettech.api.solutions_clinic.model.usecase.user;

import com.jettech.api.solutions_clinic.model.entity.User;
import com.jettech.api.solutions_clinic.model.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultUpdateUserBlockedUseCase implements UpdateUserBlockedUseCase {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse execute(UpdateUserBlockedRequest request) throws AuthenticationFailedException {
        log.info("Atualizando bloqueio do usuário - userId: {}, blocked: {}", request.id(), request.blocked());
        User user = userRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Usuário", request.id()));

        user.setBlocked(request.blocked());
        user = userRepository.save(user);
        log.info("Status de bloqueio do usuário atualizado - userId: {}, blocked: {}", user.getId(), user.isBlocked());

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
