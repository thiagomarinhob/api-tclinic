package com.jettech.api.solutions_clinic.model.usecase.memed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String token;
    private String error;
    
    public TokenResponse(String token) {
        this.token = token;
    }
}
