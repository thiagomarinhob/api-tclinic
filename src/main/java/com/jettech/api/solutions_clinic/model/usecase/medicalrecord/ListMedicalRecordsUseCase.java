package com.jettech.api.solutions_clinic.model.usecase.medicalrecord;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ListMedicalRecordsUseCase {

    /**
     * Lista prontuários do tenant. Se o usuário logado for profissional da clínica,
     * retorna apenas os prontuários criados por ele; caso contrário (clínica/admin/recp),
     * retorna todos do tenant.
     */
    Page<MedicalRecordListResponse> execute(
        int page,
        int size,
        String patientName,
        LocalDateTime dateFrom,
        LocalDateTime dateTo
    ) throws AuthenticationFailedException;
}
