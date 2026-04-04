package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExamTypeRepository extends JpaRepository<ExamType, UUID> {
    List<ExamType> findByActiveTrueOrderByCategoryAscDisplayOrderAsc();
}
