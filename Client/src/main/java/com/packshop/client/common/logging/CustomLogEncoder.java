package com.packshop.client.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.PatternLayoutEncoderBase;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CustomLogEncoder extends PatternLayoutEncoderBase<ILoggingEvent> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Override
    public byte[] encode(ILoggingEvent event) {
        StringBuilder formattedLog = new StringBuilder();

        // Timestamp
        String timestamp = DATE_FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp()));

        // Get original message
        String message = event.getFormattedMessage();

        // Extract error details (loại bỏ stack trace)
        String errorDetails = extractErrorDetails(message);

        // Format the log entry
        formattedLog.append(String.format("[%s] ", timestamp))
                .append(String.format("%-5s | ", event.getLevel()))
                .append(errorDetails)
                .append("\n");

        return formattedLog.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String extractErrorDetails(String message) {
        if (message == null || message.isEmpty()) {
            return "Unknown error occurred";
        }

        // Tách các dòng trong thông điệp
        String[] lines = message.split("\n");
        if (lines.length > 0) {
            String firstLine = lines[0].trim();

            // Kiểm tra các trường hợp lỗi cụ thể và thêm chi tiết nếu cần
            for (String line : lines) {
                if (line.contains("Connection refused")) {
                    return String.format("%s [%s]", firstLine, "Service Unavailable - Connection Refused");
                }
                if (line.contains("NullPointerException")) {
                    return String.format("%s [%s]", firstLine, "Unexpected null value encountered");
                }
                if (line.contains("SQLException")) {
                    return String.format("%s [%s]", firstLine, "Database error occurred");
                }
            }

            // Chỉ lấy dòng đầu tiên (bỏ stack trace)
            return firstLine;
        }

        return message;
    }

    @Override
    public void start() {
        super.start();
    }
}