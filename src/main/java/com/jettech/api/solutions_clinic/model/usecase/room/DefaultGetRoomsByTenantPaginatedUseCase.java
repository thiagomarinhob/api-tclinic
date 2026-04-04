package com.jettech.api.solutions_clinic.model.usecase.room;

import com.jettech.api.solutions_clinic.model.entity.Room;
import com.jettech.api.solutions_clinic.model.repository.RoomRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.security.TenantContext;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetRoomsByTenantPaginatedUseCase implements GetRoomsByTenantPaginatedUseCase {

    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public Page<RoomResponse> execute(GetRoomsByTenantPaginatedRequest request) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(request.tenantId());
        tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Clínica", request.tenantId()));

        Pageable pageable = createPageable(request.page(), request.size(), request.sort());

        Page<Room> roomsPage;
        if (request.active() == null) {
            roomsPage = roomRepository.findByTenantId(request.tenantId(), pageable);
        } else {
            roomsPage = roomRepository.findByTenantIdAndIsActive(request.tenantId(), request.active(), pageable);
        }

        return roomsPage.map(this::toResponse);
    }

    private Pageable createPageable(int page, int size, String sort) {
        Sort sortObj;
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            String direction = parts.length > 1 ? parts[1].trim().toUpperCase() : "ASC";
            Sort.Direction dir = "DESC".equals(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sortObj = Sort.by(dir, field);
        } else {
            sortObj = Sort.by(Sort.Direction.ASC, "name");
        }
        return PageRequest.of(page, size, sortObj);
    }

    private RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getTenant().getId(),
                room.getName(),
                room.getDescription(),
                room.getCapacity(),
                room.isActive(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }
}
