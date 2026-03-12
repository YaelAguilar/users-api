package com.chakray.usersapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(
            regexp = "^(\\+?\\d{1,3}[\\s-]?)?(\\d{10})$",
            message = "Phone must be 10 digits and may include country code"
    )
    private String phone;

    @NotBlank
    private String password;

    @NotBlank
    @Pattern(
            regexp = "^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$",
            message = "tax_id must have RFC format"
    )
    private String tax_id;
}