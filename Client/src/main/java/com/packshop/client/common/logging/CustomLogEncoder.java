package com.packshop.client.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.PatternLayoutEncoderBase;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CustomLogEncoder extends PatternLayoutEncoderBase<ILoggingEvent> {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    @Override
    public byte[] encode(ILoggingEvent event) {
        StringBuilder formattedLog = new StringBuilder();

        // Timestamp
        String timestamp = DATE_FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp()));

        // Get original message
        String message = event.getFormattedMessage();

        // Extract error details
        String errorDetails = extractErrorDetails(message);

        // Format the log entry
        formattedLog.append(String.format("[%s] ", timestamp))
                .append("CLIENT-SERVICE | ")
                .append(String.format("%-5s | ", event.getLevel()))
                .append(errorDetails)
                .append("\n");

        return formattedLog.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String extractErrorDetails(String message) {
        if (message == null || message.isEmpty()) {
            return "Unknown error occurred";
        }

        // Nếu có exception stack trace, chỉ lấy thông tin lỗi chính
        String[] lines = message.split("\n");
        if (lines.length > 0) {
            String firstLine = lines[0].trim();

            // Nếu có exception detail trong các dòng tiếp theo
            for (String line : lines) {
                if (line.contains("Connection refused")) {
                    return String.format("%s [%s]",
                            firstLine,
                            "Service Unavailable - Connection Refused"
                    );
                }
            }
            return firstLine;
        }

        return message;
    }

    @Override
    public void start() {
        super.start();
    }
}