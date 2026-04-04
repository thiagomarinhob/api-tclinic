package com.jettech.api.solutions_clinic.model.usecase.user;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.jettech.api.solutions_clinic.model.repository.UserRepository;
import com.jettech.api.solutions_clinic.model.repository.UserTenantRoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultAuthUserUseCase implements AuthUserUseCase {


    private final UserRepository userRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.token.secret}")
    private String secretKey;

    @Value("${security.jwt.issuer:solutions-clinic}")
    private String issuer;

    @Override
    @Transactional(readOnly = true)
    public AuthUserResponse execute(AuthUserRequest authUserRequest) throws AuthenticationFailedException {
        var user = this.userRepository.findByEmail(authUserRequest.email()).orElseThrow(() -> {
            throw new UsernameNotFoundException("Username/password invalido");
        });

        var passwordMatches = passwordEncoder.matches(authUserRequest.password(), user.getPassword());

        if(!passwordMatches) {
            throw new AuthenticationFailedException();
        }

        // Buscar o tenantId ativo do usuário (preferir tenant ativo, senão o primeiro disponível)
        var userTenantRoles = userTenantRoleRepository.findByUser_Id(user.getId());
        UUID tenantId = null;
        
        if (!userTenantRoles.isEmpty()) {
            // Tentar encontrar um tenant ativo primeiro
            var activeTenantRole = userTenantRoles.stream()
                    .filter(utr -> utr.getTenant().isActive())
                    .findFirst();
            
            if (activeTenantRole.isPresent()) {
                tenantId = activeTenantRole.get().getTenant().getId();
            } else {
                // Se não houver tenant ativo, usar o primeiro disponível
                tenantId = userTenantRoles.get(0).getTenant().getId();
            }
        }

        var expiresIn = Instant.now().plus(Duration.ofDays(1));
        Algorithm  algorithm = Algorithm.HMAC256(secretKey);
        var tokenBuilder = JWT.create()
                .withIssuer(issuer)
                .withExpiresAt(expiresIn)
                .withSubject(user.getId().toString());
        
        // Adicionar clinicId ao token se disponível
        if (tenantId != null) {
            tokenBuilder.withClaim("clinicId", tenantId.toString());
        }
        
        var token = tokenBuilder.sign(algorithm);

        return new AuthUserResponse(token, expiresIn.toEpochMilli());

    }
}
