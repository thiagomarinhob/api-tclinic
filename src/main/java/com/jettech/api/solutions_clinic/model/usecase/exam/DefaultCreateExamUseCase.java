package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.Exam;
import com.jettech.api.solutions_clinic.model.entity.ExamStatus;
import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.ExamRepository;
import com.jettech.api.solutions_clinic.model.repository.PatientRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateExamUseCase implements CreateExamUseCase {

    private final ExamRepository examRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public ExamResponse execute(CreateExamRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", tenantId));

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new EntityNotFoundException("Paciente", request.patientId()));
        if (!patient.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException();
        }

        Appointment appointment = null;
        if (request.appointmentId() != null) {
            appointment = appointmentRepository.findById(request.appointmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Consulta", request.appointmentId()));
            if (!appointment.getTenant().getId().equals(tenantId)) {
                throw new ForbiddenException();
            }
        }

        Exam exam = new Exam();
        exam.setTenant(tenant);
        exam.setPatient(patient);
        exam.setAppointment(appointment);
        exam.setName(request.name());
        exam.setClinicalIndication(request.clinicalIndication());
        exam.setStatus(ExamStatus.REQUESTED);

        exam = examRepository.save(exam);

        return toResponse(exam);
    }

    static ExamResponse toResponse(Exam exam) {
        return new ExamResponse(
                exam.getId(),
                exam.getTenant().getId(),
                exam.getPatient().getId(),
                exam.getAppointment() != null ? exam.getAppointment().getId() : null,
                exam.getName(),
                exam.getClinicalIndication(),
                exam.getStatus(),
                exam.getResultFileKey(),
                exam.getRequestFileKey(),
                exam.getCreatedAt(),
                exam.getUpdatedAt()
        );
    }
}
