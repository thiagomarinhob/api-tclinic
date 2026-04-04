package com.jettech.api.solutions_clinic.model.usecase.user;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.jettech.api.solutions_clinic.model.entity.UserTenantRole;
import com.jettech.api.solutions_clinic.model.repository.UserRepository;
import com.jettech.api.solutions_clinic.model.repository.UserTenantRoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultSwitchTenantUseCase implements SwitchTenantUseCase {

    private final UserRepository userRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;

    @Value("${security.token.secret}")
    private String secretKey;

    @Value("${security.jwt.issuer:solutions-clinic}")
    private String issuer;

    @Override
    @Transactional(readOnly = true)
    public AuthUserResponse execute(SwitchTenantRequest request) throws AuthenticationFailedException {
        UUID userId = getUserIdFromContext();
        if (userId == null) {
            throw new AuthenticationFailedException();
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationFailedException());

        List<UserTenantRole> userTenantRoles = userTenantRoleRepository.findByUser_IdAndTenant_Id(userId, request.tenantId());
        if (userTenantRoles.isEmpty()) {
            throw new AuthenticationFailedException();
        }

        UserTenantRole selected = userTenantRoles.get(0);
        if (!selected.getTenant().isActive()) {
            throw new AuthenticationFailedException();
        }

        var expiresIn = Instant.now().plus(Duration.ofDays(1));
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        var tokenBuilder = JWT.create()
                .withIssuer(issuer)
                .withExpiresAt(expiresIn)
                .withSubject(userId.toString())
                .withClaim("clinicId", request.tenantId().toString());

        var token = tokenBuilder.sign(algorithm);

        return new AuthUserResponse(token, expiresIn.toEpochMilli());
    }

    private UUID getUserIdFromContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                String sub = jwt.getSubject();
                if (sub != null && !sub.isBlank()) {
                    return UUID.fromString(sub);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
