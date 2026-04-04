package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);

    long countByTenantIdAndReadFalse(UUID tenantId);

    Optional<Notification> findByIdAndTenantId(UUID id, UUID tenantId);

    @Modifying
    @Query("UPDATE notifications n SET n.read = true WHERE n.tenant.id = :tenantId AND n.read = false")
    int markAllAsReadByTenantId(@Param("tenantId") UUID tenantId);
}
