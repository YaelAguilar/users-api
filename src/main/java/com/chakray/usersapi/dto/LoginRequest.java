package com.chakray.usersapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    private String tax_id;

    @NotBlank
    private String password;
}