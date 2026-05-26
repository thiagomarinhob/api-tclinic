package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.jettech.api.solutions_clinic.model.usecase.appointment.AppointmentFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultSaveTriageUseCaseTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock TenantContext tenantContext;
    @Mock AppointmentResponseMapper mapper;

    @InjectMocks
    DefaultSaveTriageUseCase useCase;

    private Appointment appointment;
    private ObjectNode vitalSigns;

    @BeforeEach
    void setUp() throws Exception {
        appointment = appointment();
        vitalSigns = JsonNodeFactory.instance.objectNode();
        vitalSigns.put("pressao", "120/80");
        vitalSigns.put("temperatura", 36.5);

        when(tenantContext.getRequiredClinicId()).thenReturn(TENANT_ID);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(mapper.toResponse(any())).thenReturn(appointmentResponse());
    }

    @Test
    void shouldSaveVitalSignsToAppointment() throws Exception {
        SaveTriageRequest request = new SaveTriageRequest(APPOINTMENT_ID, vitalSigns);

        useCase.execute(request);

        assertThat(appointment.getVitalSigns()).isEqualTo(vitalSigns);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldReturnMappedResponse() throws Exception {
        AppointmentResponse expected = appointmentResponse();
        when(mapper.toResponse(any())).thenReturn(expected);
        SaveTriageRequest request = new SaveTriageRequest(APPOINTMENT_ID, vitalSigns);

        AppointmentResponse result = useCase.execute(request);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldOverwriteExistingVitalSigns() throws Exception {
        ObjectNode oldSigns = JsonNodeFactory.instance.objectNode();
        oldSigns.put("pressao", "130/90");
        appointment.setVitalSigns(oldSigns);

        ObjectNode newSigns = JsonNodeFactory.instance.objectNode();
        newSigns.put("pressao", "120/80");
        SaveTriageRequest request = new SaveTriageRequest(APPOINTMENT_ID, newSigns);

        useCase.execute(request);

        assertThat(appointment.getVitalSigns()).isEqualTo(newSigns);
    }

    @Test
    void shouldThrowEntityNotFoundWhenAppointmentDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(appointmentRepository.findById(unknownId)).thenReturn(Optional.empty());
        SaveTriageRequest request = new SaveTriageRequest(unknownId, vitalSigns);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowForbiddenWhenAppointmentBelongsToDifferentTenant() throws Exception {
        when(tenantContext.getRequiredClinicId()).thenReturn(OTHER_TENANT_ID);
        SaveTriageRequest request = new SaveTriageRequest(APPOINTMENT_ID, vitalSigns);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(ForbiddenException.class);
    }
}
