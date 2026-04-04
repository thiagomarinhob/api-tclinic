package com.jettech.api.solutions_clinic.model.usecase.room;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;

import java.util.List;

public interface GetRoomsByTenantUseCase extends UseCase<GetRoomsByTenantRequest, List<RoomResponse>> {
}
