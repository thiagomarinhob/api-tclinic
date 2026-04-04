package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.MedicalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, UUID> {
    
    List<MedicalDocument> findByAppointmentId(UUID appointmentId);
    
    List<MedicalDocument> findByAppointmentIdAndSource(UUID appointmentId, String source);
}
