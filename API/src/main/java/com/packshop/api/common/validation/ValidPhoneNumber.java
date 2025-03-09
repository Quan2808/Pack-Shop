package com.packshop.api.common.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

@Pattern(regexp = "^\\(\\+84\\)\\s[0-9]{3}\\s[0-9]{3}\\s[0-9]{3}$", message = "Phone number must be in the format (+84) 123 456 789")
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {
  String message() default "Invalid phone number";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}