package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.entity.ConsentType;
import com.jettech.api.solutions_clinic.model.usecase.consent.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PatientConsentController implements PatientConsentAPI {

    private final GrantConsentUseCase grantConsentUseCase;
    private final RevokeConsentUseCase revokeConsentUseCase;
    private final GetPatientConsentsUseCase getPatientConsentsUseCase;
    private final HttpServletRequest httpRequest;

    @Override
    public PatientConsentResponse grantConsent(
            @PathVariable UUID patientId,
            @Valid @RequestBody GrantConsentBodyRequest body) throws AuthenticationFailedException {
        String ip = resolveClientIp();
        return grantConsentUseCase.execute(new GrantConsentRequest(patientId, body.consentType(), body.termVersion(), ip));
    }

    @Override
    public PatientConsentResponse revokeConsent(
            @PathVariable UUID patientId,
            @PathVariable UUID consentId) throws AuthenticationFailedException {
        return revokeConsentUseCase.execute(new RevokeConsentRequest(patientId, consentId));
    }

    @Override
    public List<PatientConsentResponse> getConsents(
            @PathVariable UUID patientId,
            @RequestParam(required = false) ConsentType consentType) throws AuthenticationFailedException {
        return getPatientConsentsUseCase.execute(new GetPatientConsentsRequest(patientId, consentType));
    }

    private String resolveClientIp() {
        String forwarded = httpRequest.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
    }
}
