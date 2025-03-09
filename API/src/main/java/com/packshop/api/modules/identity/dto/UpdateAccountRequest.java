package com.packshop.api.modules.identity.dto;

import com.packshop.api.common.validation.ValidEmail;
import com.packshop.api.common.validation.ValidFieldName;
import com.packshop.api.common.validation.ValidPath;
import com.packshop.api.common.validation.ValidPhoneNumber;

import lombok.Data;

@Data
public class UpdateAccountRequest {
    @ValidEmail
    private String email;

    @ValidFieldName(fieldName = "Full nane")
    private String fullName;

    @ValidPhoneNumber
    private String phoneNumber;

    @ValidPath(maxLength = 255)
    private String avatarUrl;
}
