package com.jettech.api.solutions_clinic.model.usecase.consent;

import com.jettech.api.solutions_clinic.exception.InvalidRequestException;
import com.jettech.api.solutions_clinic.model.entity.BloodType;
import com.jettech.api.solutions_clinic.model.entity.Gender;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.PatientConsentRepository;
import com.jettech.api.solutions_clinic.model.repository.PatientRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.model.usecase.patient.CreatePatientRequest;
import com.jettech.api.solutions_clinic.model.usecase.patient.DefaultCreatePatientUseCase;
import com.jettech.api.solutions_clinic.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultCreatePatientConsentValidationTest {

    static final UUID TENANT_ID = UUID.randomUUID();

    @Mock PatientRepository patientRepository;
    @Mock TenantRepository tenantRepository;
    @Mock PatientConsentRepository patientConsentRepository;
    @Mock TenantContext tenantContext;

    @InjectMocks DefaultCreatePatientUseCase useCase;

    @Test
    void shouldThrowInvalidRequestWhenTreatmentConsentIsNull() throws Exception {
        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant()));

        assertThatThrownBy(() -> useCase.execute(requestWithConsent(null)))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldThrowInvalidRequestWhenGrantedIsFalse() throws Exception {
        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant()));

        assertThatThrownBy(() -> useCase.execute(requestWithConsent(new TreatmentConsentRequest(false, "1.0"))))
                .isInstanceOf(InvalidRequestException.class);
    }

    private static Tenant tenant() {
        Tenant t = new Tenant();
        t.setId(TENANT_ID);
        t.setName("Clínica Teste");
        return t;
    }

    private static CreatePatientRequest requestWithConsent(TreatmentConsentRequest consent) {
        return new CreatePatientRequest(
                TENANT_ID, "João", null, null, null,
                Gender.MASCULINO, null, null, null,
                null, null, null, null, null, null, null,
                BloodType.A_POSITIVE, null, null, null, null, null,
                consent
        );
    }
}
