package com.jettech.api.solutions_clinic.model.usecase.room;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;
import org.springframework.data.domain.Page;

public interface GetRoomsByTenantPaginatedUseCase extends UseCase<GetRoomsByTenantPaginatedRequest, Page<RoomResponse>> {
}
