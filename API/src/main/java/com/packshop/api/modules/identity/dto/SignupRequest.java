package com.packshop.api.modules.identity.dto;

import com.packshop.api.common.validation.ValidEmail;
import com.packshop.api.common.validation.ValidFieldName;
import com.packshop.api.common.validation.ValidPassword;
import com.packshop.api.common.validation.ValidPath;
import com.packshop.api.common.validation.ValidPhoneNumber;
import com.packshop.api.common.validation.ValidUniqueName;

import lombok.Data;

@Data
public class SignupRequest {
    @ValidUniqueName(fieldName = "Username")
    private String username;

    @ValidPassword
    private String password;

    @ValidEmail
    private String email;

    @ValidFieldName(fieldName = "Full nane")
    private String fullName;

    @ValidPhoneNumber
    private String phoneNumber;

    @ValidPath(maxLength = 255)
    private String avatarUrl;
}
