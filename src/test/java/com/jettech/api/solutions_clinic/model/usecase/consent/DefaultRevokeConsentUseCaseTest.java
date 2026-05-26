package com.jettech.api.solutions_clinic.model.usecase.consent;

import com.jettech.api.solutions_clinic.exception.DuplicateEntityException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.entity.ConsentType;
import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.entity.PatientConsent;
import com.jettech.api.solutions_clinic.model.repository.PatientConsentRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.jettech.api.solutions_clinic.model.usecase.consent.DefaultGrantConsentUseCaseTest.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultRevokeConsentUseCaseTest {

    static final UUID CONSENT_ID = UUID.randomUUID();
    static final UUID OTHER_PATIENT_ID = UUID.randomUUID();

    @Mock PatientConsentRepository patientConsentRepository;
    @Mock TenantContext tenantContext;

    @InjectMocks DefaultRevokeConsentUseCase useCase;

    @Test
    void shouldSetRevokedAtAndReturnResponse() throws Exception {
        Patient patient = patientWithTenant(TENANT_ID);
        PatientConsent consent = activeConsent(patient);

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(patientConsentRepository.findById(CONSENT_ID)).thenReturn(Optional.of(consent));
        when(patientConsentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PatientConsentResponse response = useCase.execute(new RevokeConsentRequest(PATIENT_ID, CONSENT_ID));

        assertThat(response.revokedAt()).isNotNull();
    }

    @Test
    void shouldThrowEntityNotFoundWhenConsentDoesNotExist() throws Exception {
        when(patientConsentRepository.findById(CONSENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new RevokeConsentRequest(PATIENT_ID, CONSENT_ID)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowEntityNotFoundWhenConsentBelongsToDifferentPatient() throws Exception {
        Patient patient = patientWithTenant(TENANT_ID);
        PatientConsent consent = activeConsent(patient);

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(patientConsentRepository.findById(CONSENT_ID)).thenReturn(Optional.of(consent));

        assertThatThrownBy(() -> useCase.execute(new RevokeConsentRequest(OTHER_PATIENT_ID, CONSENT_ID)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowDuplicateEntityWhenAlreadyRevoked() throws Exception {
        Patient patient = patientWithTenant(TENANT_ID);
        PatientConsent consent = activeConsent(patient);
        consent.setRevokedAt(LocalDateTime.now().minusDays(1));

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(patientConsentRepository.findById(CONSENT_ID)).thenReturn(Optional.of(consent));

        assertThatThrownBy(() -> useCase.execute(new RevokeConsentRequest(PATIENT_ID, CONSENT_ID)))
                .isInstanceOf(DuplicateEntityException.class);
    }

    private static PatientConsent activeConsent(Patient patient) {
        PatientConsent c = new PatientConsent();
        c.setId(CONSENT_ID);
        c.setPatient(patient);
        c.setConsentType(ConsentType.TREATMENT);
        c.setGranted(true);
        c.setGrantedAt(LocalDateTime.now());
        return c;
    }
}
