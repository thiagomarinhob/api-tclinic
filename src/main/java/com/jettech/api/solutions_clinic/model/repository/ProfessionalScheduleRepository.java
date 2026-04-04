package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.ProfessionalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalScheduleRepository extends JpaRepository<ProfessionalSchedule, UUID> {
    
    List<ProfessionalSchedule> findByProfessionalId(UUID professionalId);
    
    Optional<ProfessionalSchedule> findByProfessionalIdAndDayOfWeek(UUID professionalId, DayOfWeek dayOfWeek);
    
    void deleteByProfessionalId(UUID professionalId);
}
