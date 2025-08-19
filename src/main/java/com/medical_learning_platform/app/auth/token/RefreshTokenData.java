package com.medical_learning_platform.app.auth.token;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshTokenData {
    private Long userId;
    private String email;
}
