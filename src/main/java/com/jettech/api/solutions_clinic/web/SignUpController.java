package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.signup.SignUpClinicOwnerUseCase;
import com.jettech.api.solutions_clinic.model.usecase.signup.SignUpSoloUseCase;
import com.jettech.api.solutions_clinic.model.usecase.signup.SignUpClinicOwnerRequest;
import com.jettech.api.solutions_clinic.model.usecase.signup.SignUpResponse;
import com.jettech.api.solutions_clinic.model.usecase.signup.SignUpSoloRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class SignUpController implements SignUpAPI {

    private final SignUpClinicOwnerUseCase signUpClinicOwnerUseCase;
    private final SignUpSoloUseCase signUpSoloUseCase;

    @Override
    public SignUpResponse signUpClinicOwner(@Valid @RequestBody SignUpClinicOwnerRequest request) throws AuthenticationFailedException {
        return signUpClinicOwnerUseCase.execute(request);
    }

    @Override
    public SignUpResponse signUpSolo(@Valid @RequestBody SignUpSoloRequest request) throws AuthenticationFailedException {
        return signUpSoloUseCase.execute(request);
    }
}

