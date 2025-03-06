package com.packshop.api.common.exceptions;

public class InvalidStatusException extends RuntimeException {
  private final String currentStatus;
  private final String expectedStatus;

  public InvalidStatusException(String entityId, String currentStatus, String expectedStatus) {
    super(String.format("Cannot process entity %s with status %s. Expected status: %s", entityId, currentStatus,
        expectedStatus));
    this.currentStatus = currentStatus;
    this.expectedStatus = expectedStatus;
  }

  public String getCurrentStatus() {
    return currentStatus;
  }

  public String getExpectedStatus() {
    return expectedStatus;
  }
}