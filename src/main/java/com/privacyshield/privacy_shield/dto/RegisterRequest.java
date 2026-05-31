package com.privacyshield.privacy_shield.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @Email(message = "Valid email do")
    @NotBlank(message = "Email required hai")
    private String email;

    @NotBlank(message = "Password required hai")
    private String password;

    private String caFirmName;
}
