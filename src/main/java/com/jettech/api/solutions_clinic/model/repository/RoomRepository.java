package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByTenantId(UUID tenantId);

    List<Room> findByTenantIdAndIsActive(UUID tenantId, boolean isActive);

    Page<Room> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Room> findByTenantIdAndIsActive(UUID tenantId, boolean isActive, Pageable pageable);

    Optional<Room> findByIdAndTenantId(UUID id, UUID tenantId);
}

