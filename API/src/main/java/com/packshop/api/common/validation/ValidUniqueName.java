package com.packshop.api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NotBlank(message = "{fieldName} is required")
@Size(min = 3, max = 50, message = "{fieldName} must be between 3 and 50 characters")
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUniqueName {
  String fieldName() default "Field";

  String message() default "Invalid {fieldName}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}