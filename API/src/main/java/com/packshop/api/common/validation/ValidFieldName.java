package com.packshop.api.common.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@NotBlank(message = "{fieldName} is required")
@Size(max = 100, message = "{fieldName} must not exceed 100 characters")
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFieldName {
  String fieldName();

  String message() default "Invalid {fieldName}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
