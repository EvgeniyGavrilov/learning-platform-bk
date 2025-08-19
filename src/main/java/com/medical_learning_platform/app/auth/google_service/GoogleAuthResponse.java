package com.medical_learning_platform.app.auth.google_service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoogleAuthResponse {
//    private String accessToken;
//    private String userId;
    private String accessToken;
    private String refreshToken;

}
