package com.jettech.api.solutions_clinic.security;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Fornece o tenant (clínica) e o usuário autenticado a partir do JWT.
 * Deve ser usado para validar que operações só acessem dados do tenant do usuário.
 */
@Component
public class TenantContext {

    private static final String CLAIM_CLINIC_ID = "clinicId";

    /**
     * Retorna o clinicId do JWT do usuário autenticado.
     * Lança AuthenticationFailedException se não houver autenticação ou claim clinicId.
     */
    public UUID getRequiredClinicId() throws AuthenticationFailedException {
        UUID clinicId = getClinicIdOrNull();
        if (clinicId == null) {
            throw new AuthenticationFailedException(ApiError.ENTITY_NOT_FOUND_CLINIC);
        }
        return clinicId;
    }

    /**
     * Retorna o clinicId do JWT ou null se não houver autenticação/clinicId.
     */
    public UUID getClinicIdOrNull() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
                return null;
            }
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String clinicIdStr = jwt.getClaimAsString(CLAIM_CLINIC_ID);
            if (clinicIdStr == null || clinicIdStr.isBlank()) {
                return null;
            }
            return UUID.fromString(clinicIdStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Garante que o tenantId informado é o mesmo do contexto (JWT).
     * Lança ForbiddenException se for diferente.
     */
    public void requireSameTenant(UUID requestTenantId) throws AuthenticationFailedException {
        UUID contextClinicId = getRequiredClinicId();
        if (!contextClinicId.equals(requestTenantId)) {
            throw new ForbiddenException(ApiError.ACCESS_DENIED);
        }
    }

    /**
     * Retorna o userId (subject do JWT) do usuário autenticado, ou null se não houver.
     */
    public UUID getUserIdOrNull() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
                return null;
            }
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String sub = jwt.getSubject();
            if (sub == null || sub.isBlank()) {
                return null;
            }
            return UUID.fromString(sub);
        } catch (Exception e) {
            return null;
        }
    }
}
