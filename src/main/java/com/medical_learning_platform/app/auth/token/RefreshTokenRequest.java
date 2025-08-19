package com.medical_learning_platform.app.auth.token;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
