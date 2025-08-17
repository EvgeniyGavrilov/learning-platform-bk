package com.medical_learning_platform.app.auth.code;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerifyCodeRequest {
    private String email;
    private String code;
}
