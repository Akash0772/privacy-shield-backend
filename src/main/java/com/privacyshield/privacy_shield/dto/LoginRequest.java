package com.privacyshield.privacy_shield.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
