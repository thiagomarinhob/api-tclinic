package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.ConsentType;
import com.jettech.api.solutions_clinic.model.entity.PatientConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PatientConsentRepository extends JpaRepository<PatientConsent, UUID> {

    List<PatientConsent> findByPatientIdOrderByGrantedAtDesc(UUID patientId);

    List<PatientConsent> findByPatientIdAndConsentTypeOrderByGrantedAtDesc(UUID patientId, ConsentType consentType);
}
