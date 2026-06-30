package com.jettech.api.solutions_clinic.model.usecase.tenant;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultUpdateConfirmationWindowUseCaseTest {

    @Mock TenantRepository tenantRepository;
    @InjectMocks DefaultUpdateConfirmationWindowUseCase useCase;

    private UUID tenantId;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        tenant = new Tenant();
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        tenant.setName("Clínica ABC");
    }

    @Test
    void whenValueIsValid_thenPersistsAndReturnsUpdatedValue() throws Exception {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(tenant)).thenReturn(tenant);

        TenantResponse response = useCase.execute(new UpdateConfirmationWindowRequest(tenantId, 90));

        assertThat(tenant.getConfirmationWindowMinutes()).isEqualTo(90);
        assertThat(response.confirmationWindowMinutes()).isEqualTo(90);
        verify(tenantRepository).save(tenant);
    }

    @Test
    void whenTenantDoesNotExist_thenThrowsEntityNotFound() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new UpdateConfirmationWindowRequest(tenantId, 90)))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
