package com.jettech.api.solutions_clinic.model.usecase.room;

import com.jettech.api.solutions_clinic.model.entity.Room;
import com.jettech.api.solutions_clinic.model.repository.RoomRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetRoomByIdUseCase implements GetRoomByIdUseCase {

    private final RoomRepository roomRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public RoomResponse execute(UUID id) throws AuthenticationFailedException {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sala", id));
        if (!room.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
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

