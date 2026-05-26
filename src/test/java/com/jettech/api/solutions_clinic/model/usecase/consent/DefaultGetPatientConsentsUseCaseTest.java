package com.jettech.api.solutions_clinic.model.usecase.consent;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.ConsentType;
import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.entity.PatientConsent;
import com.jettech.api.solutions_clinic.model.repository.PatientConsentRepository;
import com.jettech.api.solutions_clinic.model.repository.PatientRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.jettech.api.solutions_clinic.model.usecase.consent.DefaultGrantConsentUseCaseTest.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultGetPatientConsentsUseCaseTest {

    @Mock PatientRepository patientRepository;
    @Mock PatientConsentRepository patientConsentRepository;
    @Mock TenantContext tenantContext;

    @InjectMocks DefaultGetPatientConsentsUseCase useCase;

    @Test
    void shouldReturnAllConsentsOrderedByGrantedAtDesc() throws Exception {
        Patient patient = patientWithTenant(TENANT_ID);
        List<PatientConsent> consents = List.of(
                consent(patient, ConsentType.MARKETING),
                consent(patient, ConsentType.TREATMENT)
        );

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(patientConsentRepository.findByPatientIdOrderByGrantedAtDesc(PATIENT_ID)).thenReturn(consents);

        List<PatientConsentResponse> result = useCase.execute(new GetPatientConsentsRequest(PATIENT_ID, null));

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListWhenNoConsentsExist() throws Exception {
        Patient patient = patientWithTenant(TENANT_ID);

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(patientConsentRepository.findByPatientIdOrderByGrantedAtDesc(PATIENT_ID)).thenReturn(List.of());

        List<PatientConsentResponse> result = useCase.execute(new GetPatientConsentsRequest(PATIENT_ID, null));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFilterByConsentTypeWhenProvided() throws Exception {
        Patient patient = patientWithTenant(TENANT_ID);
        List<PatientConsent> consents = List.of(consent(patient, ConsentType.TREATMENT));

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(patientConsentRepository.findByPatientIdAndConsentTypeOrderByGrantedAtDesc(PATIENT_ID, ConsentType.TREATMENT))
                .thenReturn(consents);

        List<PatientConsentResponse> result = useCase.execute(new GetPatientConsentsRequest(PATIENT_ID, ConsentType.TREATMENT));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).consentType()).isEqualTo(ConsentType.TREATMENT);
        verify(patientConsentRepository, never()).findByPatientIdOrderByGrantedAtDesc(any());
    }

    @Test
    void shouldThrowEntityNotFoundWhenPatientDoesNotExist() throws Exception {
        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GetPatientConsentsRequest(PATIENT_ID, null)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowForbiddenWhenPatientBelongsToDifferentTenant() throws Exception {
        Patient patient = patientWithTenant(UUID.randomUUID());

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> useCase.execute(new GetPatientConsentsRequest(PATIENT_ID, null)))
                .isInstanceOf(ForbiddenException.class);
    }

    private static PatientConsent consent(Patient patient, ConsentType type) {
        PatientConsent c = new PatientConsent();
        c.setId(UUID.randomUUID());
        c.setPatient(patient);
        c.setConsentType(type);
        c.setGranted(true);
        c.setGrantedAt(LocalDateTime.now());
        return c;
    }
}
