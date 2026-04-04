package com.jettech.api.solutions_clinic.model.usecase.room;

import com.jettech.api.solutions_clinic.model.entity.Room;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.RoomRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.security.TenantContext;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateRoomUseCase implements CreateRoomUseCase {

    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public RoomResponse execute(CreateRoomRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", tenantId));

        Room room = new Room();
        room.setTenant(tenant);
        room.setName(request.name());
        room.setDescription(request.description());
        room.setCapacity(request.capacity());
        room.setActive(true);

        room = roomRepository.save(room);

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

