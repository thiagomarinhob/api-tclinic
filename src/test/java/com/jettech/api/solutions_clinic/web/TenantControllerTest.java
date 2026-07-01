package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.model.service.R2StorageService;
import com.jettech.api.solutions_clinic.model.usecase.subscription.CreateCheckoutSessionUseCase;
import com.jettech.api.solutions_clinic.model.usecase.tenant.ActivatePlanUseCase;
import com.jettech.api.solutions_clinic.model.usecase.tenant.StartTrialUseCase;
import com.jettech.api.solutions_clinic.model.usecase.tenant.TenantResponse;
import com.jettech.api.solutions_clinic.model.usecase.tenant.UpdateConfirmationWindowUseCase;
import com.jettech.api.solutions_clinic.model.usecase.tenant.UpdateTenantPlanUseCase;
import com.jettech.api.solutions_clinic.model.usecase.user.AssociateUserToTenantUseCase;
import com.jettech.api.solutions_clinic.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantControllerTest {

    @Mock UpdateTenantPlanUseCase updateTenantPlanUseCase;
    @Mock CreateCheckoutSessionUseCase createCheckoutSessionUseCase;
    @Mock AssociateUserToTenantUseCase associateUserToTenantUseCase;
    @Mock ActivatePlanUseCase activatePlanUseCase;
    @Mock StartTrialUseCase startTrialUseCase;
    @Mock UpdateConfirmationWindowUseCase updateConfirmationWindowUseCase;
    @Mock TenantRepository tenantRepository;
    @Mock TenantContext tenantContext;
    @Mock R2StorageService r2StorageService;

    @InjectMocks TenantController controller;

    private UUID tenantId;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        tenant = new Tenant();
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        tenant.setName("Clínica ABC");
        tenant.setConfirmationWindowMinutes(90);
    }

    @Test
    void whenGettingCurrentTenant_thenResponseIncludesConfiguredConfirmationWindow() throws Exception {
        when(tenantContext.getRequiredClinicId()).thenReturn(tenantId);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        TenantResponse response = controller.getCurrentTenant();

        assertThat(response.confirmationWindowMinutes()).isEqualTo(90);
    }
}
