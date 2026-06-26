package com.jettech.api.solutions_clinic.model.usecase.admin;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.InvalidStateException;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.entity.TypeTenant;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultAdminSuspendTenantUseCaseTest {

    @Mock TenantRepository tenantRepository;
    @InjectMocks DefaultAdminSuspendTenantUseCase useCase;

    private UUID tenantId;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        tenant = new Tenant();
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        tenant.setName("Clínica ABC");
        tenant.setType(TypeTenant.CLINIC);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin-uuid", null, List.of()));
    }

    @Test
    void whenTenantIsActive_thenSuspendSuccessfully() {
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setActive(true);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        useCase.execute(tenantId);

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
        assertThat(tenant.isActive()).isFalse();
        verify(tenantRepository).save(tenant);
    }

    @Test
    void whenTenantIsTrial_thenSuspendSuccessfully() {
        tenant.setStatus(TenantStatus.TRIAL);
        tenant.setActive(true);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        useCase.execute(tenantId);

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
        verify(tenantRepository).save(tenant);
    }

    @Test
    void whenTenantAlreadySuspended_thenThrowInvalidStateException() {
        tenant.setStatus(TenantStatus.SUSPENDED);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> useCase.execute(tenantId))
                .isInstanceOf(InvalidStateException.class);
        verify(tenantRepository, never()).save(any());
    }

    @Test
    void whenTenantIsCanceled_thenThrowInvalidStateException() {
        tenant.setStatus(TenantStatus.CANCELED);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> useCase.execute(tenantId))
                .isInstanceOf(InvalidStateException.class);
        verify(tenantRepository, never()).save(any());
    }

    @Test
    void whenTenantIsPendingSetup_thenThrowInvalidStateException() {
        tenant.setStatus(TenantStatus.PENDING_SETUP);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> useCase.execute(tenantId))
                .isInstanceOf(InvalidStateException.class);
        verify(tenantRepository, never()).save(any());
    }

    @Test
    void whenTenantNotFound_thenThrowEntityNotFoundException() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(tenantId))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
