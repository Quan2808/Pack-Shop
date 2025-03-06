package com.packshop.api.common.exceptions;

public class InvalidStatusException extends RuntimeException {
  private final String entityId;
  private final String currentStatus;
  private final String expectedStatus;
  private final String invalidValue;
  private final Class<?> enumClass;

  public InvalidStatusException(String entityId, String invalidValue, Class<?> enumClass) {
    super(String.format("Invalid status '%s' for entity %s. Valid values are: %s",
        invalidValue, entityId, getEnumValues(enumClass)));
    this.entityId = entityId;
    this.currentStatus = null;
    this.expectedStatus = null;
    this.invalidValue = invalidValue;
    this.enumClass = enumClass;
  }

  public InvalidStatusException(String entityId, String currentStatus, String expectedStatus, Class<?> enumClass) {
    super(String.format("Cannot process entity %s with current status '%s'. Expected status: '%s'",
        entityId, currentStatus, expectedStatus));
    this.entityId = entityId;
    this.currentStatus = currentStatus;
    this.expectedStatus = expectedStatus;
    this.invalidValue = null;
    this.enumClass = enumClass;
  }

  private static String getEnumValues(Class<?> enumClass) {
    if (enumClass == null || !enumClass.isEnum()) {
      return "unknown";
    }
    Object[] enumConstants = enumClass.getEnumConstants();
    return String.join(", ",
        java.util.Arrays.stream(enumConstants)
            .map(Object::toString)
            .toArray(String[]::new));
  }

  public String getEntityId() {
    return entityId;
  }

  public String getCurrentStatus() {
    return currentStatus;
  }

  public String getExpectedStatus() {
    return expectedStatus;
  }

  public String getInvalidValue() {
    return invalidValue;
  }

  public Class<?> getEnumClass() {
    return enumClass;
  }
}