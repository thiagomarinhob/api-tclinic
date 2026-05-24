package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.InvalidStateException;
import com.jettech.api.solutions_clinic.model.entity.Procedure;
import com.jettech.api.solutions_clinic.model.repository.ProcedureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.jettech.api.solutions_clinic.model.usecase.appointment.AppointmentFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcedureLoaderTest {

    @Mock ProcedureRepository procedureRepository;

    @InjectMocks
    ProcedureLoader loader;

    private Procedure activeProcedure;

    @BeforeEach
    void setUp() {
        activeProcedure = procedure(PROCEDURE_ID, true, 30, new BigDecimal("150.00"));
        when(procedureRepository.findByIdAndTenantId(PROCEDURE_ID, TENANT_ID))
                .thenReturn(Optional.of(activeProcedure));
    }

    @Test
    void shouldLoadSingleProcedureAndCalculateTotals() {
        ProcedureLoadResult result = loader.loadAndValidate(List.of(PROCEDURE_ID), TENANT_ID);

        assertThat(result.procedures()).containsExactly(activeProcedure);
        assertThat(result.totalDurationMinutes()).isEqualTo(30);
        assertThat(result.totalValueFromProcedures()).isEqualByComparingTo("150.00");
    }

    @Test
    void shouldSumDurationAndValueForMultipleProcedures() {
        UUID secondId = UUID.randomUUID();
        Procedure second = procedure(secondId, true, 20, new BigDecimal("80.00"));
        when(procedureRepository.findByIdAndTenantId(secondId, TENANT_ID))
                .thenReturn(Optional.of(second));

        ProcedureLoadResult result = loader.loadAndValidate(
                List.of(PROCEDURE_ID, secondId), TENANT_ID
        );

        assertThat(result.procedures()).hasSize(2);
        assertThat(result.totalDurationMinutes()).isEqualTo(50);
        assertThat(result.totalValueFromProcedures()).isEqualByComparingTo("230.00");
    }

    @Test
    void shouldThrowEntityNotFoundWhenProcedureDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(procedureRepository.findByIdAndTenantId(unknownId, TENANT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> loader.loadAndValidate(List.of(unknownId), TENANT_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowEntityNotFoundWhenProcedureDoesNotBelongToTenant() {
        UUID otherTenant = UUID.randomUUID();
        when(procedureRepository.findByIdAndTenantId(PROCEDURE_ID, otherTenant))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> loader.loadAndValidate(List.of(PROCEDURE_ID), otherTenant))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowInvalidStateWhenProcedureIsInactive() {
        Procedure inactive = procedure(PROCEDURE_ID, false, 30, new BigDecimal("150.00"));
        when(procedureRepository.findByIdAndTenantId(PROCEDURE_ID, TENANT_ID))
                .thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> loader.loadAndValidate(List.of(PROCEDURE_ID), TENANT_ID))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void shouldStopAtFirstInactiveProcedureAndNotLoadTheRest() {
        UUID secondId = UUID.randomUUID();
        Procedure inactive = procedure(PROCEDURE_ID, false, 30, new BigDecimal("150.00"));
        when(procedureRepository.findByIdAndTenantId(PROCEDURE_ID, TENANT_ID))
                .thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> loader.loadAndValidate(List.of(PROCEDURE_ID, secondId), TENANT_ID))
                .isInstanceOf(InvalidStateException.class);

        // O segundo procedimento nunca deve ser consultado
        verify(procedureRepository, never()).findByIdAndTenantId(secondId, TENANT_ID);
    }

    @Test
    void shouldReturnZeroTotalsForEmptyList() {
        ProcedureLoadResult result = loader.loadAndValidate(List.of(), TENANT_ID);

        assertThat(result.procedures()).isEmpty();
        assertThat(result.totalDurationMinutes()).isZero();
        assertThat(result.totalValueFromProcedures()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
