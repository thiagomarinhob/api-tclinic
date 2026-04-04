package com.jettech.api.solutions_clinic.model.usecase.user;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;

public interface UpdateUserUseCase extends UseCase<UpdateUserRequest, UserResponse> {

    boolean checkCpfExists(String cpf);
}
