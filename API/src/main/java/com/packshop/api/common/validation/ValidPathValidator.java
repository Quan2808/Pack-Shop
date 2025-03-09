package com.packshop.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPathValidator implements ConstraintValidator<ValidPath, String> {

  private int maxLength;

  @Override
  public void initialize(ValidPath constraintAnnotation) {
    this.maxLength = constraintAnnotation.maxLength();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    if (value.length() > maxLength) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
          "Avatar URL must not exceed " + maxLength + " characters")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}