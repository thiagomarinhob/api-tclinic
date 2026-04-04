package com.jettech.api.solutions_clinic.web;


import com.jettech.api.solutions_clinic.model.usecase.user.AuthUserRequest;
import com.jettech.api.solutions_clinic.model.usecase.user.AuthUserResponse;
import com.jettech.api.solutions_clinic.model.usecase.user.AuthUserUseCase;
import com.jettech.api.solutions_clinic.model.usecase.user.SwitchTenantUseCase;
import com.jettech.api.solutions_clinic.model.usecase.user.SwitchTenantRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AuthUserController implements AuthUserAPI {

    private final AuthUserUseCase authUserUseCase;
    private final SwitchTenantUseCase switchTenantUseCase;

    @Override
    public AuthUserResponse signIn(@Valid @RequestBody AuthUserRequest authUserRequest) throws AuthenticationFailedException {
        return authUserUseCase.execute(authUserRequest);
    }

    @Override
    public AuthUserResponse switchTenant(@Valid @RequestBody SwitchTenantRequest request) throws AuthenticationFailedException {
        return switchTenantUseCase.execute(request);
    }
}
