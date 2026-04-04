package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.medicalrecord.CreateOrUpdateMedicalRecordRequest;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecord.ExportMedicalRecordPdfUseCase;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecord.GetMedicalRecordByAppointmentUseCase;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecord.GetMedicalRecordByIdUseCase;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecord.ListMedicalRecordsUseCase;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecord.MedicalRecordListResponse;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecord.MedicalRecordResponse;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecord.CreateOrUpdateMedicalRecordUseCase;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecord.SignMedicalRecordUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MedicalRecordController implements MedicalRecordAPI {

    private final CreateOrUpdateMedicalRecordUseCase createOrUpdateUseCase;
    private final GetMedicalRecordByAppointmentUseCase getByAppointmentUseCase;
    private final GetMedicalRecordByIdUseCase getByIdUseCase;
    private final ListMedicalRecordsUseCase listUseCase;
    private final SignMedicalRecordUseCase signUseCase;
    private final ExportMedicalRecordPdfUseCase exportPdfUseCase;

    @Override
    public Page<MedicalRecordListResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) throws AuthenticationFailedException {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;
        return listUseCase.execute(page, size, patientName, from, to);
    }

    @Override
    public MedicalRecordResponse createOrUpdate(@Valid @RequestBody CreateOrUpdateMedicalRecordRequest request) throws AuthenticationFailedException {
        return createOrUpdateUseCase.execute(request);
    }

    @Override
    public ResponseEntity<MedicalRecordResponse> getByAppointment(@PathVariable UUID appointmentId) throws AuthenticationFailedException {
        return getByAppointmentUseCase.execute(appointmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Override
    public MedicalRecordResponse getById(@PathVariable UUID id) throws AuthenticationFailedException {
        return getByIdUseCase.execute(id);
    }

    @Override
    public MedicalRecordResponse sign(@PathVariable UUID id) throws AuthenticationFailedException {
        return signUseCase.execute(id);
    }

    @Override
    public ResponseEntity<byte[]> exportPdf(@PathVariable UUID id) throws AuthenticationFailedException {
        byte[] pdf = exportPdfUseCase.execute(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"prontuario-" + id + ".pdf\"");
        headers.setContentLength(pdf.length);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
