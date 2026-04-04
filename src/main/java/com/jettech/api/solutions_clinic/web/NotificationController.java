package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.entity.Notification;
import com.jettech.api.solutions_clinic.model.repository.NotificationRepository;
import com.jettech.api.solutions_clinic.model.usecase.notification.NotificationResponse;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class NotificationController implements NotificationAPI {

    private final NotificationRepository notificationRepository;
    private final TenantContext tenantContext;

    @Override
    public List<NotificationResponse> getNotifications(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(tenantId);
        var pageable = PageRequest.of(page, size);
        var notifications = notificationRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
        return notifications.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(@PathVariable UUID tenantId) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(tenantId);
        return notificationRepository.countByTenantIdAndReadFalse(tenantId);
    }

    @Override
    public void markAsRead(@PathVariable UUID tenantId, @PathVariable UUID id) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(tenantId);
        Notification notification = notificationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Notificação", id));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(@PathVariable UUID tenantId) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(tenantId);
        notificationRepository.markAllAsReadByTenantId(tenantId);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTenant().getId(),
                n.getType(),
                n.getTitle(),
                n.getDescription(),
                n.isRead(),
                n.getReferenceType(),
                n.getReferenceId(),
                n.getCreatedAt()
        );
    }
}
