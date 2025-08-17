package com.medical_learning_platform.app.auth.google_service;

import lombok.Data;

@Data
public class GoogleUserInfo {
    private String id;
    private String email;
    private String name;
    private String picture;
}
