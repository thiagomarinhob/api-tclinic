package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.ChamadaPainel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChamadaPainelRepository extends JpaRepository<ChamadaPainel, UUID> {

    long countByAppointmentId(UUID appointmentId);

    @Query("SELECT c FROM ChamadaPainel c JOIN FETCH c.appointment a JOIN FETCH a.patient JOIN FETCH c.room " +
           "WHERE cast(c.horaChamada as date) = current_date ORDER BY c.horaChamada DESC")
    List<ChamadaPainel> findChamadasHoje();
}
