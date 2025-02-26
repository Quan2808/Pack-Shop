package com.packshop.api.modules.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAccountRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Pattern(regexp = "^\\(\\+84\\)\\s[0-9]{3}\\s[0-9]{3}\\s[0-9]{3}$",
            message = "Phone number must be in the format (+84) 123 456 789")
    private String phoneNumber;

    @Size(max = 255, message = "Avatar URL must not exceed 255 characters")
    private String avatarUrl;
}
