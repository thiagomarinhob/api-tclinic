package com.jettech.api.solutions_clinic.model.usecase.consent;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.ConsentType;
import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.entity.PatientConsent;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.PatientConsentRepository;
import com.jettech.api.solutions_clinic.model.repository.PatientRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultGrantConsentUseCaseTest {

    static final UUID TENANT_ID = UUID.randomUUID();
    static final UUID PATIENT_ID = UUID.randomUUID();
    static final UUID OTHER_TENANT_ID = UUID.randomUUID();

    @Mock PatientRepository patientRepository;
    @Mock PatientConsentRepository patientConsentRepository;
    @Mock TenantContext tenantContext;

    @InjectMocks DefaultGrantConsentUseCase useCase;

    @Test
    void shouldPersistConsentAndReturnResponse() throws Exception {
        Patient patient = patientWithTenant(TENANT_ID);
        PatientConsent saved = savedConsent(patient, ConsentType.TREATMENT, "1.0");

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(patientConsentRepository.save(any())).thenReturn(saved);

        var request = new GrantConsentRequest(PATIENT_ID, ConsentType.TREATMENT, "1.0", "127.0.0.1");
        PatientConsentResponse response = useCase.execute(request);

        assertThat(response.patientId()).isEqualTo(PATIENT_ID);
        assertThat(response.consentType()).isEqualTo(ConsentType.TREATMENT);
        assertThat(response.granted()).isTrue();
        assertThat(response.termVersion()).isEqualTo("1.0");
        assertThat(response.revokedAt()).isNull();

        ArgumentCaptor<PatientConsent> captor = ArgumentCaptor.forClass(PatientConsent.class);
        verify(patientConsentRepository).save(captor.capture());
        assertThat(captor.getValue().getGrantedAt()).isNotNull();
        assertThat(captor.getValue().getIpAddress()).isEqualTo("127.0.0.1");
    }

    @Test
    void shouldThrowEntityNotFoundWhenPatientDoesNotExist() throws Exception {
        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GrantConsentRequest(PATIENT_ID, ConsentType.TREATMENT, "1.0", null)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowForbiddenWhenPatientBelongsToDifferentTenant() throws Exception {
        Patient patient = patientWithTenant(OTHER_TENANT_ID);

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> useCase.execute(new GrantConsentRequest(PATIENT_ID, ConsentType.TREATMENT, "1.0", null)))
                .isInstanceOf(ForbiddenException.class);
    }

    static Patient patientWithTenant(UUID tenantId) {
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        Patient patient = new Patient();
        patient.setId(PATIENT_ID);
        patient.setTenant(tenant);
        return patient;
    }

    static PatientConsent savedConsent(Patient patient, ConsentType type, String version) {
        PatientConsent c = new PatientConsent();
        c.setId(UUID.randomUUID());
        c.setPatient(patient);
        c.setConsentType(type);
        c.setGranted(true);
        c.setGrantedAt(LocalDateTime.now());
        c.setTermVersion(version);
        return c;
    }
}
