package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.room.CreateRoomRequest;
import com.jettech.api.solutions_clinic.model.usecase.room.CreateRoomUseCase;
import com.jettech.api.solutions_clinic.model.usecase.room.GetRoomByIdUseCase;
import com.jettech.api.solutions_clinic.model.usecase.room.GetRoomsByTenantPaginatedRequest;
import com.jettech.api.solutions_clinic.model.usecase.room.GetRoomsByTenantPaginatedUseCase;
import com.jettech.api.solutions_clinic.model.usecase.room.GetRoomsByTenantRequest;
import com.jettech.api.solutions_clinic.model.usecase.room.GetRoomsByTenantUseCase;
import com.jettech.api.solutions_clinic.model.usecase.room.RoomResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RoomController implements RoomAPI {

    private final CreateRoomUseCase createRoomUseCase;
    private final GetRoomByIdUseCase getRoomByIdUseCase;
    private final GetRoomsByTenantUseCase getRoomsByTenantUseCase;
    private final GetRoomsByTenantPaginatedUseCase getRoomsByTenantPaginatedUseCase;

    @Override
    public RoomResponse createRoom(@Valid @RequestBody CreateRoomRequest request) throws AuthenticationFailedException {
        return createRoomUseCase.execute(request);
    }

    @Override
    public RoomResponse getRoomById(@PathVariable UUID id) throws AuthenticationFailedException {
        return getRoomByIdUseCase.execute(id);
    }

    @Override
    public Page<RoomResponse> getRoomsByTenantPaginated(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "name,asc") String sort
    ) throws AuthenticationFailedException {
        return getRoomsByTenantPaginatedUseCase.execute(
                new GetRoomsByTenantPaginatedRequest(tenantId, active, page, size, sort));
    }

    @Override
    public List<RoomResponse> getRoomsByTenant(
            @RequestParam UUID tenantId,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly
    ) throws AuthenticationFailedException {
        return getRoomsByTenantUseCase.execute(new GetRoomsByTenantRequest(tenantId, activeOnly));
    }
}

