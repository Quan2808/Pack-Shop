package com.packshop.api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NotBlank(message = "Email is required")
@Email(message = "Email must be valid")
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmail {
  String message() default "Invalid email";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}